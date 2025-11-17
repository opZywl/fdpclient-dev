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
import net.ccbluex.liquidbounce.handler.spotify.SpotifyIntegration
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Standalone Spotify integration that fetches the currently playing track from the Spotify Web API.
 */
object SpotifyModule : Module("Spotify", Category.CLIENT, defaultState = false) {

    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val service: SpotifyService
        get() = SpotifyIntegration.service
    private var workerJob: Job? = null
    private var browserAuthFuture: CompletableFuture<SpotifyAccessToken>? = null
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
        reloadCredentialsFromDisk()
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
        browserAuthFuture?.cancel(true)
        browserAuthFuture = null
        updateConnection(SpotifyConnectionState.DISCONNECTED, null)
    }

    fun openConfigScreen() {
        reloadCredentialsFromDisk()
        mc.displayGuiScreen(GuiSpotify(mc.currentScreen))
    }

    fun updateCredentials(clientId: String, clientSecret: String, refreshToken: String): Boolean {
        val sanitized = SpotifyCredentials(
            clientId.trim(),
            clientSecret.trim(),
            refreshToken.trim(),
        )

        LOGGER.info(
            "[Spotify] Received credential update (clientId=${mask(sanitized.clientId)}, refreshToken=${mask(sanitized.refreshToken)})"
        )

        if (!sanitized.isValid()) {
            LOGGER.warn("[Spotify] Ignoring credential update because at least one field is blank")
            return false
        }

        clientIdValue.set(sanitized.clientId)
        clientSecretValue.set(sanitized.clientSecret)
        refreshTokenValue.set(sanitized.refreshToken)
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

    fun beginBrowserAuthorization(callback: (BrowserAuthStatus, String) -> Unit): Boolean {
        val clientId = clientIdValue.get().trim()
        val clientSecret = clientSecretValue.get().trim()
        if (clientId.isBlank() || clientSecret.isBlank()) {
            callback(BrowserAuthStatus.ERROR, "Enter the client ID and secret before authorizing.")
            return false
        }

        val ongoing = browserAuthFuture
        if (ongoing != null && !ongoing.isDone) {
            callback(BrowserAuthStatus.INFO, "Browser authorization is already running.")
            return false
        }

        callback(BrowserAuthStatus.INFO, "Opening Spotify authorization flow in your browser...")
        val future = SpotifyIntegration.authorizeInBrowser(clientId, clientSecret)
        browserAuthFuture = future
        future.whenComplete { token, throwable ->
            mc.addScheduledTask {
                browserAuthFuture = null
                if (throwable != null) {
                    callback(BrowserAuthStatus.ERROR, "Authorization failed: ${throwable.message}")
                    return@addScheduledTask
                }

                if (token == null || token.refreshToken.isNullOrBlank()) {
                    callback(BrowserAuthStatus.ERROR, "Spotify did not return a refresh token.")
                    return@addScheduledTask
                }

                refreshTokenValue.set(token.refreshToken)
                cachedToken = token
                val saved = persistCredentials()
                if (saved) {
                    callback(BrowserAuthStatus.SUCCESS, "Authorization completed. Credentials saved.")
                } else {
                    callback(BrowserAuthStatus.ERROR, "Authorization succeeded but saving failed. Check the logs.")
                }

                if (state) {
                    workerJob?.cancel()
                    workerJob = null
                    startWorker()
                }
            }
        }

        return true
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
        currentState = null
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
        ensureCredentialsDirectory()
        LOGGER.info("[Spotify] Loading credentials from ${credentialsFile.absolutePath}")
        if (!credentialsFile.exists()) {
            cachedToken = null
            LOGGER.info("[Spotify] No saved credentials found at ${credentialsFile.absolutePath}")
            return false
        }

        return runCatching {
            val json = credentialsFile.readText(StandardCharsets.UTF_8)
            if (json.isBlank()) {
                cachedToken = null
                return@runCatching false
            }

            val element = JsonParser().parse(json)
            if (!element.isJsonObject) {
                cachedToken = null
                return@runCatching false
            }

            val obj = element.asJsonObject
            obj.get(CONFIG_KEY_CLIENT_ID)?.takeIf { it.isJsonPrimitive }?.asString?.let { clientIdValue.set(it) }
            obj.get(CONFIG_KEY_CLIENT_SECRET)?.takeIf { it.isJsonPrimitive }?.asString?.let { clientSecretValue.set(it) }
            obj.get(CONFIG_KEY_REFRESH_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString?.let { refreshTokenValue.set(it) }

            val restoredToken = obj.get(CONFIG_KEY_ACCESS_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString
            val restoredExpiry = obj.get(CONFIG_KEY_ACCESS_TOKEN_EXPIRY)?.takeIf { it.isJsonPrimitive }?.asLong ?: 0L
            cachedToken = if (!restoredToken.isNullOrBlank() && restoredExpiry > System.currentTimeMillis()) {
                LOGGER.info(
                    "[Spotify] Restored cached access token from disk (expires in ${(restoredExpiry - System.currentTimeMillis()) / 1000}s)"
                )
                SpotifyAccessToken(restoredToken, restoredExpiry)
            } else {
                if (!restoredToken.isNullOrBlank()) {
                    LOGGER.info("[Spotify] Ignoring expired cached access token from disk")
                }
                null
            }

            LOGGER.info(
                "[Spotify] Loaded credentials from ${credentialsFile.absolutePath} (clientId=${mask(clientIdValue.get())}, refreshToken=${mask(refreshTokenValue.get())})"
            )
            true
        }.onFailure {
            cachedToken = null
            LOGGER.warn("[Spotify] Failed to load saved credentials", it)
        }.getOrDefault(false)
    }

    private fun persistCredentials(): Boolean {
        val credentials = SpotifyCredentials(clientIdValue.get(), clientSecretValue.get(), refreshTokenValue.get())
        if (!credentials.isValid()) {
            LOGGER.warn("[Spotify] Refusing to persist invalid credentials")
            return false
        }

        return runCatching {
            val directory = credentialsFile.parentFile ?: FileManager.dir
            if (!directory.exists() && !directory.mkdirs()) {
                throw IllegalStateException("Unable to create directory: ${directory.absolutePath}")
            }

            val tokenSnapshot = cachedToken
            LOGGER.info(
                "[Spotify] Persisting credentials to ${credentialsFile.absolutePath} (clientId=${mask(credentials.clientId)}, refreshToken=${mask(credentials.refreshToken)}, accessToken=${maskToken(tokenSnapshot)})"
            )

            val payload = JsonObject().apply {
                addProperty(CONFIG_KEY_CLIENT_ID, credentials.clientId)
                addProperty(CONFIG_KEY_CLIENT_SECRET, credentials.clientSecret)
                addProperty(CONFIG_KEY_REFRESH_TOKEN, credentials.refreshToken)
                addProperty(CONFIG_KEY_ACCESS_TOKEN, tokenSnapshot?.value ?: "")
                addProperty(CONFIG_KEY_ACCESS_TOKEN_EXPIRY, tokenSnapshot?.expiresAtMillis ?: 0L)
            }

            FileManager.writeFile(credentialsFile, FileManager.PRETTY_GSON.toJson(payload))
            LOGGER.info(
                "[Spotify] Saved credentials to ${credentialsFile.absolutePath} (${credentialsFile.length()} bytes written)"
            )
        }.onFailure {
            LOGGER.warn("[Spotify] Failed to save credentials", it)
        }.isSuccess
    }

    private fun ensureCredentialsDirectory() {
        val directory = credentialsFile.parentFile ?: FileManager.dir
        if (!directory.exists() && directory.mkdirs()) {
            LOGGER.info("[Spotify] Created credentials directory at ${directory.absolutePath}")
        }
    }

    private fun mask(value: String): String = when {
        value.isEmpty() -> "<empty>"
        value.length <= 4 -> "***"
        value.length <= 8 -> value.take(2) + "***"
        else -> value.take(4) + "***" + value.takeLast(2)
    }

    private fun maskToken(token: SpotifyAccessToken?): String = token?.value?.let(::mask) ?: "<none>"

    private const val RETRY_DELAY_MS = 5_000L
    private val TOKEN_EXPIRY_GRACE_MS = TimeUnit.SECONDS.toMillis(5)

    private const val CONFIG_KEY_CLIENT_ID = "clientId"
    private const val CONFIG_KEY_CLIENT_SECRET = "clientSecret"
    private const val CONFIG_KEY_REFRESH_TOKEN = "refreshToken"
    private const val CONFIG_KEY_ACCESS_TOKEN = "accessToken"
    private const val CONFIG_KEY_ACCESS_TOKEN_EXPIRY = "accessTokenExpiryMillis"

    enum class BrowserAuthStatus {
        INFO,
        SUCCESS,
        ERROR,
    }
}
