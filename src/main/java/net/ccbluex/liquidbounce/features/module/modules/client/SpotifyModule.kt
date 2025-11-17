/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.gui.GuiSpotify
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyAccessToken
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyCredentials
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyDefaults
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyService
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyStateChangedEvent
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Standalone Spotify integration that fetches the currently playing track from the Spotify Web API.
 */
object SpotifyModule : Module("Spotify", Category.CLIENT, defaultState = false) {

    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val service = SpotifyService()
    private var workerJob: Job? = null
    private var cachedToken: SpotifyAccessToken? = null
    private val credentialsFile = File(FileManager.dir, "spotify.json")

    private val clientIdValue = text("ClientId", SpotifyDefaults.clientId).apply { hide() }
    private val clientSecretValue = text("ClientSecret", SpotifyDefaults.clientSecret).apply { hide() }
    private val refreshTokenValue = text("RefreshToken", SpotifyDefaults.refreshToken).apply { hide() }
    private val pollIntervalValue = int("PollInterval", SpotifyDefaults.pollIntervalSeconds, 3..60, suffix = "s")
    private val autoReconnectValue = boolean("AutoReconnect", true)

    init {
        loadSavedCredentials()
    }

    @Volatile
    var currentState: SpotifyState? = null
        private set

    @Volatile
    var connectionState: SpotifyConnectionState = SpotifyConnectionState.DISCONNECTED
        private set

    @Volatile
    var lastErrorMessage: String? = null
        private set

    val pollIntervalSeconds: Int
        get() = pollIntervalValue.get()

    val autoReconnect: Boolean
        get() = autoReconnectValue.get()

    val clientId: String
        get() = clientIdValue.get()

    val clientSecret: String
        get() = clientSecretValue.get()

    val refreshToken: String
        get() = refreshTokenValue.get()

    override fun onEnable() {
        updateConnection(SpotifyConnectionState.CONNECTING, null)
        startWorker()
        if (!hasCredentials()) {
            chat("§cSpotify credentials are missing. Open the configuration screen to enter them.")
            mc.displayGuiScreen(GuiSpotify(mc.currentScreen))
        }
    }

    override fun onDisable() {
        workerJob?.cancel()
        workerJob = null
        cachedToken = null
        updateConnection(SpotifyConnectionState.DISCONNECTED, null)
    }

    fun openConfigScreen() {
        reloadCredentialsFromDisk()
        mc.displayGuiScreen(GuiSpotify(mc.currentScreen))
    }

    fun updateCredentials(clientId: String, clientSecret: String, refreshToken: String): Boolean {
        LOGGER.info(
            "[Spotify] Received credential update (clientId=${mask(clientId)}, refreshToken=${mask(refreshToken)})"
        )
        clientIdValue.set(clientId)
        clientSecretValue.set(clientSecret)
        refreshTokenValue.set(refreshToken)
        val saved = persistCredentials()
        cachedToken = null
        if (state) {
            workerJob?.cancel()
            workerJob = null
            startWorker()
        }
        return saved
    }

    fun setPollInterval(seconds: Int) {
        pollIntervalValue.set(seconds.coerceIn(3, 60))
    }

    fun toggleAutoReconnect(): Boolean {
        autoReconnectValue.toggle()
        return autoReconnectValue.get()
    }

    private fun hasCredentials(): Boolean = SpotifyCredentials(clientId, clientSecret, refreshToken).isValid()

    private fun startWorker() {
        if (workerJob?.isActive == true) {
            return
        }

        workerJob = moduleScope.launch {
            while (this@SpotifyModule.state) {
                val credentials = SpotifyCredentials(clientId, clientSecret, refreshToken)
                if (!credentials.isValid()) {
                    handleError("Missing Spotify credentials")
                    delay(RETRY_DELAY_MS)
                    continue
                }

                val token = ensureAccessToken(credentials)
                if (token == null) {
                    delay(RETRY_DELAY_MS)
                    continue
                }

                runCatching { service.fetchCurrentlyPlaying(token.value) }
                    .onFailure { handleError("Failed to fetch playback: ${'$'}{it.message}") }
                    .onSuccess { state ->
                        currentState = state
                        EventManager.call(SpotifyStateChangedEvent(state))
                        updateConnection(SpotifyConnectionState.CONNECTED, null)
                    }

                delay(TimeUnit.SECONDS.toMillis(pollIntervalSeconds.toLong()))
            }
        }
    }

    private suspend fun ensureAccessToken(credentials: SpotifyCredentials): SpotifyAccessToken? {
        val cached = cachedToken
        if (cached != null && cached.expiresAtMillis > System.currentTimeMillis() + TOKEN_EXPIRY_GRACE_MS) {
            return cached
        }

        return runCatching { service.refreshAccessToken(credentials) }
            .onSuccess {
                cachedToken = it
                updateConnection(SpotifyConnectionState.CONNECTED, null)
            }
            .onFailure {
                handleError("Failed to refresh Spotify token: ${'$'}{it.message}")
            }
            .getOrNull()
    }

    private fun handleError(message: String) {
        LOGGER.warn("[Spotify] $message")
        updateConnection(SpotifyConnectionState.ERROR, message)
        if (!autoReconnect) {
            chat("§cSpotify module disabled: $message")
            state = false
        }
    }

    private fun updateConnection(state: SpotifyConnectionState, error: String?) {
        if (connectionState == state && lastErrorMessage == error) {
            return
        }

        connectionState = state
        lastErrorMessage = error
        EventManager.call(SpotifyConnectionChangedEvent(state, error))
    }

    fun reloadCredentialsFromDisk(): Boolean = loadSavedCredentials()

    fun credentialsFilePath(): String = credentialsFile.absolutePath

    private fun loadSavedCredentials(): Boolean {
        LOGGER.info("[Spotify] Loading credentials from ${credentialsFile.absolutePath}")
        if (!credentialsFile.exists()) {
            LOGGER.info("[Spotify] No saved credentials found at ${credentialsFile.absolutePath}")
            return false
        }

        return runCatching {
            val json = credentialsFile.readText(StandardCharsets.UTF_8)
            if (json.isBlank()) {
                return@runCatching false
            }

            val element = JsonParser().parse(json)
            if (!element.isJsonObject) {
                return@runCatching false
            }

            val obj = element.asJsonObject
            clientIdValue.set(obj.get("clientId")?.asString ?: "")
            clientSecretValue.set(obj.get("clientSecret")?.asString ?: "")
            refreshTokenValue.set(obj.get("refreshToken")?.asString ?: "")
            LOGGER.info("[Spotify] Loaded credentials from ${credentialsFile.absolutePath}")
            true
        }.onFailure {
            LOGGER.warn("[Spotify] Failed to load saved credentials", it)
        }.getOrDefault(false)
    }

    private fun persistCredentials(): Boolean {
        return runCatching {
            val directory = credentialsFile.parentFile ?: FileManager.dir
            if (!directory.exists() && !directory.mkdirs()) {
                throw IllegalStateException("Unable to create directory: ${directory.absolutePath}")
            }

            LOGGER.info(
                "[Spotify] Persisting credentials to ${credentialsFile.absolutePath} (clientId=${mask(clientIdValue.get())}, refreshToken=${mask(refreshTokenValue.get())})"
            )

            val payload = JsonObject().apply {
                addProperty("clientId", clientIdValue.get())
                addProperty("clientSecret", clientSecretValue.get())
                addProperty("refreshToken", refreshTokenValue.get())
            }

            FileManager.writeFile(credentialsFile, FileManager.PRETTY_GSON.toJson(payload))
            LOGGER.info(
                "[Spotify] Saved credentials to ${credentialsFile.absolutePath} (${credentialsFile.length()} bytes written)"
            )
        }.onFailure {
            LOGGER.warn("[Spotify] Failed to save credentials", it)
        }.isSuccess
    }

    private fun mask(value: String): String = when {
        value.isEmpty() -> "<empty>"
        value.length <= 4 -> "***"
        value.length <= 8 -> value.take(2) + "***"
        else -> value.take(4) + "***" + value.takeLast(2)
    }

    private const val RETRY_DELAY_MS = 5_000L
    private val TOKEN_EXPIRY_GRACE_MS = TimeUnit.SECONDS.toMillis(5)
}
