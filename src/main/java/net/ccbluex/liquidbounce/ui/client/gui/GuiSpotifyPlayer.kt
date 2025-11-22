/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.handler.spotify.SpotifyIntegration
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyPlaylistSummary
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyStateChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyTrack
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyTrackPage
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.resources.I18n
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import okhttp3.Response
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.UUID
import kotlin.math.max

class GuiSpotifyPlayer(private val prevScreen: GuiScreen?) : AbstractScreen(), Listenable {

    private lateinit var searchField: GuiTextField
    private lateinit var backButton: GuiButton
    private lateinit var refreshButton: GuiButton
    private lateinit var playPauseButton: GuiButton
    private lateinit var previousButton: GuiButton
    private lateinit var nextButton: GuiButton

    private var playlists: List<SpotifyPlaylistSummary> = emptyList()
    private var playlistsLoading = false
    private var playlistError: String? = null

    private var selectedPlaylist: SpotifyPlaylistSummary? = null
    private val trackCache = mutableMapOf<String, SpotifyTrackPage>()
    private var displayedTracks: List<SpotifyTrack> = emptyList()
    private var filteredTracks: List<SpotifyTrack> = emptyList()
    private var tracksLoading = false
    private var tracksError: String? = null

    private var playlistScroll = 0f
    private var trackScroll = 0f
    private var searchQuery = ""
    private var selectedTrackIndex = -1
    private var lastTrackClickIndex = -1
    private var lastTrackClickTime = 0L

    private var playbackState: SpotifyState? = SpotifyModule.currentState
    private var connectionState: SpotifyConnectionState = SpotifyModule.connectionState
    private var listening = false

    private var coverTexture: ResourceLocation? = null
    private var coverUrl: String? = null
    private val coverCache = mutableMapOf<String, ResourceLocation>()

    private var bannerMessage: String? = null
    private var bannerExpiry = 0L

    private val stateHandler = handler<SpotifyStateChangedEvent>(always = true) { event ->
        playbackState = event.state
        updateCoverTexture(event.state)
        updatePlayPauseLabel()
    }

    private val connectionHandler = handler<SpotifyConnectionChangedEvent>(always = true) { event ->
        connectionState = event.state
    }

    override fun handleEvents(): Boolean = listening

    override fun initGui() {
        super.initGui()
        Keyboard.enableRepeatEvents(true)
        listening = true
        buttonList.clear()
        textFields.clear()

        val searchWidth = width - 56
        searchField = textField(401, mc.fontRendererObj, 28, HEADER_HEIGHT - 36, searchWidth, 18)
        searchField.maxStringLength = 80

        backButton = +GuiButton(BUTTON_BACK, 20, height - 28, 80, 20, I18n.format("gui.back"))
        refreshButton = +GuiButton(BUTTON_REFRESH, width - 104, HEADER_HEIGHT - 38, 84, 20, "Reload")
        previousButton = +GuiButton(BUTTON_PREVIOUS, width / 2 - 90, height - 60, 40, 20, "⏮")
        playPauseButton = +GuiButton(BUTTON_PLAY_PAUSE, width / 2 - 40, height - 60, 80, 20, resolvePlayPauseLabel())
        nextButton = +GuiButton(BUTTON_NEXT, width / 2 + 50, height - 60, 40, 20, "⏭")

        if (playlists.isEmpty()) {
            reloadPlaylists(force = true)
        } else {
            updateTrackFilters()
        }
        updateCoverTexture(playbackState)
        SpotifyModule.requestPlaybackRefresh()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        Keyboard.enableRepeatEvents(false)
        listening = false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawGradientRect(0, 0, width, height, 0xFF0B0B0B.toInt(), 0xFF080808.toInt())
        drawTopChrome()

        searchField.drawTextBox()
        if (searchField.text.isEmpty() && !searchField.isFocused) {
            mc.fontRendererObj.drawString("Search playlists or tracks", searchField.xPosition + 4, searchField.yPosition + 6, 0xFF777777.toInt())
        }

        drawPlaylists(mouseX, mouseY)
        drawTracks(mouseX, mouseY)
        drawPlaybackBar()
        drawBanner()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawConnectionBadge() {
        val status = connectionState.displayName
        val color = when (connectionState) {
            SpotifyConnectionState.CONNECTED -> 0xFF1DB954.toInt()
            SpotifyConnectionState.CONNECTING -> 0xFFE5A041.toInt()
            SpotifyConnectionState.ERROR -> 0xFFE55959.toInt()
            SpotifyConnectionState.DISCONNECTED -> 0xFFB0B0B0.toInt()
        }
        val text = "Status: $status"
        val textWidth = mc.fontRendererObj.getStringWidth(text)
        val badgeLeft = width - textWidth - 34
        val badgeTop = 18
        Gui.drawRect(badgeLeft - 10, badgeTop - 4, badgeLeft + textWidth + 10, badgeTop + 14, 0x66000000)
        Gui.drawRect(badgeLeft - 10, badgeTop + 12, badgeLeft + textWidth + 10, badgeTop + 13, color)
        mc.fontRendererObj.drawString(text, badgeLeft, badgeTop, color)
    }

    private fun drawTopChrome() {
        drawGradientRect(0, 0, width, HEADER_HEIGHT, 0xFF111111.toInt(), 0xFF0A0A0A.toInt())
        drawAmbientCover()
        Gui.drawRect(0, HEADER_HEIGHT - 2, width, HEADER_HEIGHT, 0xFF1DB954.toInt())

        val greeting = "Good morning"
        mc.fontRendererObj.drawString(greeting, 28, 16, 0xFFFFFFFF.toInt())
        mc.fontRendererObj.drawString("Browse and play like Spotify", 28, 28, 0xFFB8B8B8.toInt())
        drawConnectionBadge()
        drawNowPlayingBadge()
        drawSearchBackdrop()
    }

    private fun drawPlaylists(mouseX: Int, mouseY: Int) {
        val area = playlistArea()
        Gui.drawRect(area.left - 1, area.top - 1, area.right + 1, area.bottom + 1, 0xFF0F0F0F.toInt())
        Gui.drawRect(area.left, area.top, area.right, area.bottom, 0xB0121212.toInt())
        Gui.drawRect(area.left, area.top - 14, area.right, area.top - 2, 0xFF0D0D0D.toInt())
        mc.fontRendererObj.drawString("Your Library", area.left + 2, area.top - 12, 0xFFE6E6E6.toInt())
        mc.fontRendererObj.drawString("Playlists and mixes", area.left + 2, area.top - 2, 0xFF6F6F6F.toInt())

        when {
            playlistsLoading -> {
                drawCenteredString(mc.fontRendererObj, "Loading playlists...", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            }
            playlistError != null -> {
                drawWrappedText(playlistError!!, area, 0xFFE55959.toInt())
            }
            playlists.isEmpty() -> {
                drawCenteredString(mc.fontRendererObj, "Link your Spotify account to load playlists.", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            }
            else -> {
                val rowHeight = PLAYLIST_ROW_HEIGHT
                val viewHeight = area.height()
                val maxScroll = max(0f, playlists.size * rowHeight - viewHeight + 6f)
                playlistScroll = playlistScroll.coerceIn(0f, maxScroll)
                var y = area.top + 4 - playlistScroll
                playlists.forEach { playlist ->
                    if (y > area.bottom) {
                        return@forEach
                    }
                    if (y + rowHeight >= area.top) {
                        val hovered = mouseX in area.left..area.right && mouseY in y.toInt()..(y + rowHeight).toInt()
                        val selected = playlist.id == selectedPlaylist?.id
                        val bgColor = when {
                            selected -> 0xAA1DB954.toInt()
                            hovered -> 0x33232323
                            else -> 0x00000000
                        }
                        if (bgColor != 0) {
                            Gui.drawRect(area.left + 1, y.toInt(), area.right - 1, (y + rowHeight).toInt(), bgColor)
                        }
                        drawPlaylistCover(playlist, area.left + 4, y.toInt() + 4, 24)
                        val trackLabel = if (playlist.trackCount == 1) "1 track" else "${playlist.trackCount} tracks"
                        mc.fontRendererObj.drawString(playlist.name, area.left + 34, y.toInt() + 4, 0xFFF8F8F8.toInt())
                        mc.fontRendererObj.drawString(trackLabel, area.left + 34, y.toInt() + 16, 0xFFBEBEBE.toInt())
                    }
                    y += rowHeight
                }
            }
        }
    }

    private fun drawTracks(mouseX: Int, mouseY: Int) {
        val area = trackArea()
        Gui.drawRect(area.left - 1, area.top - 1, area.right + 1, area.bottom + 1, 0xFF0F0F0F.toInt())
        Gui.drawRect(area.left, area.top, area.right, area.bottom, 0xB0131313.toInt())
        val playlist = selectedPlaylist
        val title = playlist?.name ?: "Select a playlist"
        mc.fontRendererObj.drawString(title, area.left, area.top - 12, 0xFFE6E6E6.toInt())

        if (!tracksError.isNullOrBlank()) {
            drawWrappedText(tracksError!!, area, 0xFFE55959.toInt())
            return
        }
        if (playlist == null) {
            drawCenteredString(mc.fontRendererObj, "Choose a playlist to load tracks.", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }
        if (tracksLoading) {
            drawCenteredString(mc.fontRendererObj, "Loading tracks...", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }
        if (filteredTracks.isEmpty()) {
            val message = if (displayedTracks.isEmpty()) "This playlist has no tracks." else "No tracks match your search."
            drawCenteredString(mc.fontRendererObj, message, (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }

        val rowHeight = TRACK_ROW_HEIGHT
        val viewHeight = area.height()
        val maxScroll = max(0f, filteredTracks.size * rowHeight - viewHeight + 8f)
        trackScroll = trackScroll.coerceIn(0f, maxScroll)

        val titleColumnWidth = (area.width() * 0.42f).toInt()
        val albumColumnWidth = (area.width() * 0.23f).toInt()
        val artistColumnWidth = (area.width() * 0.24f).toInt()
        val durationColumnX = area.right - 48

        drawTrackHeaders(area, titleColumnWidth, albumColumnWidth, artistColumnWidth, durationColumnX)

        var y = area.top + 4 - trackScroll
        filteredTracks.forEachIndexed { index, track ->
            if (y > area.bottom) {
                return@forEachIndexed
            }
            if (y + rowHeight >= area.top) {
                val hovered = mouseX in area.left..area.right && mouseY in y.toInt()..(y + rowHeight).toInt()
                val isSelected = index == selectedTrackIndex
                val isPlaying = playbackState?.track?.id == track.id
                val bgColor = when {
                    isPlaying -> 0xAA1DB954.toInt()
                    isSelected -> 0x55404040
                    hovered -> 0x33202020
                    else -> 0
                }
                if (bgColor != 0) {
                    Gui.drawRect(area.left + 1, y.toInt(), area.right - 1, (y + rowHeight).toInt(), bgColor)
                }
                val baseY = y.toInt() + 4
                mc.fontRendererObj.drawString((index + 1).toString(), area.left + 6, baseY, 0xFFAAAAAA.toInt())
                drawTrackCover(track, area.left + 16, baseY - 2, 18)
                mc.fontRendererObj.drawString(trimToWidth(track.title, titleColumnWidth - 20), area.left + 40, baseY, 0xFFF0F0F0.toInt())
                mc.fontRendererObj.drawString(trimToWidth(track.album, albumColumnWidth - 12), area.left + 40 + titleColumnWidth, baseY, 0xFFB0B0B0.toInt())
                mc.fontRendererObj.drawString(trimToWidth(track.artists, artistColumnWidth - 10), area.left + 40 + titleColumnWidth + albumColumnWidth, baseY, 0xFFB0B0B0.toInt())
                mc.fontRendererObj.drawString(formatDuration(track.durationMs), durationColumnX, baseY, 0xFFB0B0B0.toInt())
            }
            y += rowHeight
        }
    }

    private fun drawTrackHeaders(area: PanelArea, titleColumnWidth: Int, albumColumnWidth: Int, artistColumnWidth: Int, durationColumnX: Int) {
        val headerTop = area.top - 2
        val headerBottom = area.top + 16
        Gui.drawRect(area.left, headerTop, area.right, headerBottom, 0xFF101010.toInt())
        Gui.drawRect(area.left, headerBottom, area.right, headerBottom + 1, 0xFF1DB954.toInt())
        mc.fontRendererObj.drawString("#", area.left + 6, area.top + 2, 0xFF888888.toInt())
        mc.fontRendererObj.drawString("Title", area.left + 38, area.top + 2, 0xFFCCCCCC.toInt())
        mc.fontRendererObj.drawString("Album", area.left + 40 + titleColumnWidth, area.top + 2, 0xFFCCCCCC.toInt())
        mc.fontRendererObj.drawString("Artist", area.left + 40 + titleColumnWidth + albumColumnWidth, area.top + 2, 0xFFCCCCCC.toInt())
        mc.fontRendererObj.drawString("Time", durationColumnX, area.top + 2, 0xFFCCCCCC.toInt())
    }

    private fun drawPlaybackBar() {
        val barTop = height - 95
        val barBottom = height - 32
        Gui.drawRect(0, barTop, width, barBottom, 0xFF0F0F0F.toInt())
        Gui.drawRect(0, barTop - 1, width, barTop, 0xFF1DB954.toInt())
        val track = playbackState?.track
        if (track == null) {
            drawCenteredString(mc.fontRendererObj, "Start playback to see the current track.", width / 2, barTop + 12, 0xFFB0B0B0.toInt())
            return
        }
        val artSize = 56
        val artX = 25
        val artY = barTop + 6
        coverTexture?.let { texture ->
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(texture)
            Gui.drawScaledCustomSizeModalRect(artX, artY, 0f, 0f, 256, 256, artSize, artSize, 256f, 256f)
        } ?: Gui.drawRect(artX, artY, artX + artSize, artY + artSize, 0xFF222222.toInt())

        val textX = artX + artSize + 10
        mc.fontRendererObj.drawString(track.title, textX, artY + 4, 0xFFFFFFFF.toInt())
        mc.fontRendererObj.drawString(track.artists, textX, artY + 18, 0xFFB0B0B0.toInt())
        mc.fontRendererObj.drawString(track.album, textX, artY + 30, 0xFF8F8F8F.toInt())

        val duration = track.durationMs.coerceAtLeast(1)
        val progress = playbackState?.progressMs ?: 0
        val ratio = (progress.toFloat() / duration).coerceIn(0f, 1f)
        val progressLeft = textX
        val progressRight = width - 40
        val progressTop = artY + artSize + 8
        val progressBottom = progressTop + 6
        Gui.drawRect(progressLeft, progressTop, progressRight, progressBottom, 0xFF1E1E1E.toInt())
        val playedWidth = (progressRight - progressLeft) * ratio
        Gui.drawRect(progressLeft, progressTop, progressLeft + playedWidth.toInt(), progressBottom, 0xFF1DB954.toInt())
        val knobX = (progressLeft + playedWidth).toInt()
        Gui.drawRect(knobX - 1, progressTop - 1, knobX + 2, progressBottom + 1, 0xFFFFFFFF.toInt())

        val elapsedText = formatDuration(progress)
        val remainingText = formatDuration(duration - progress)
        mc.fontRendererObj.drawString(elapsedText, progressLeft, progressBottom + 4, 0xFFB0B0B0.toInt())
        val remainingWidth = mc.fontRendererObj.getStringWidth(remainingText)
        mc.fontRendererObj.drawString(remainingText, progressRight - remainingWidth, progressBottom + 4, 0xFFB0B0B0.toInt())
    }

    private fun drawBanner() {
        val message = bannerMessage
        if (message.isNullOrBlank()) {
            return
        }
        if (System.currentTimeMillis() > bannerExpiry) {
            bannerMessage = null
            return
        }
        val y = height - 24
        Gui.drawRect(width / 2 - 110, y - 4, width / 2 + 110, y + 14, 0xAA000000.toInt())
        drawCenteredString(mc.fontRendererObj, message, width / 2, y, 0xFFFFFFFF.toInt())
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        if (wheel == 0) {
            return
        }
        val scaledX = Mouse.getEventX() * width / mc.displayWidth
        val scaledY = height - Mouse.getEventY() * height / mc.displayHeight - 1
        val delta = (wheel / 120f) * 18f
        when {
            playlistArea().contains(scaledX, scaledY) -> adjustPlaylistScroll(-delta)
            trackArea().contains(scaledX, scaledY) -> adjustTrackScroll(-delta)
        }
    }

    private fun adjustPlaylistScroll(delta: Float) {
        val area = playlistArea()
        val maxScroll = max(0f, playlists.size * PLAYLIST_ROW_HEIGHT - area.height() + 6f)
        playlistScroll = (playlistScroll + delta).coerceIn(0f, maxScroll)
    }

    private fun adjustTrackScroll(delta: Float) {
        val area = trackArea()
        val maxScroll = max(0f, filteredTracks.size * TRACK_ROW_HEIGHT - area.height() + 8f)
        trackScroll = (trackScroll + delta).coerceIn(0f, maxScroll)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            updateSearchQuery(searchField.text)
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton)
        if (mouseButton == 0) {
            when {
                playlistArea().contains(mouseX, mouseY) -> handlePlaylistClick(mouseY)
                trackArea().contains(mouseX, mouseY) -> handleTrackClick(mouseY)
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun handlePlaylistClick(mouseY: Int) {
        val area = playlistArea()
        val relativeY = mouseY - area.top + playlistScroll
        if (relativeY < 0) {
            return
        }
        val index = (relativeY / PLAYLIST_ROW_HEIGHT).toInt()
        if (index in playlists.indices) {
            val playlist = playlists[index]
            if (playlist.id != selectedPlaylist?.id) {
                selectedPlaylist = playlist
                selectedTrackIndex = -1
                trackScroll = 0f
                tracksError = null
                loadTracksFor(playlist, forceReload = false)
            }
        }
    }

    private fun handleTrackClick(mouseY: Int) {
        if (filteredTracks.isEmpty()) {
            return
        }
        val area = trackArea()
        val relativeY = mouseY - area.top + trackScroll
        if (relativeY < 0) {
            return
        }
        val index = (relativeY / TRACK_ROW_HEIGHT).toInt()
        if (index !in filteredTracks.indices) {
            return
        }
        if (index == lastTrackClickIndex && System.currentTimeMillis() - lastTrackClickTime < 300L) {
            playTrack(filteredTracks[index])
        } else {
            selectedTrackIndex = index
            lastTrackClickIndex = index
            lastTrackClickTime = System.currentTimeMillis()
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            BUTTON_BACK -> {
                listening = false
                mc.displayGuiScreen(prevScreen)
            }
            BUTTON_REFRESH -> reloadPlaylists(force = true)
            BUTTON_PLAY_PAUSE -> togglePlayback()
            BUTTON_PREVIOUS -> skipTrack(previous = true)
            BUTTON_NEXT -> skipTrack(previous = false)
        }
    }

    private fun reloadPlaylists(force: Boolean) {
        playlistsLoading = true
        playlistError = null
        playlistScroll = 0f
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken(forceRefresh = force)
            if (token == null) {
                playlistError = "Link your Spotify account and authorize the module."
                playlistsLoading = false
                return@launch
            }
            val result = runCatching {
                SpotifyIntegration.service.fetchUserPlaylists(token.value)
            }
            val likedInfo = runCatching { SpotifyIntegration.service.fetchSavedTracks(token.value, 1, 0) }.getOrNull()
            result.onSuccess { loaded ->
                val likedEntry = SpotifyPlaylistSummary(
                    id = LIKED_SONGS_ID,
                    name = "Liked Songs",
                    description = "Your saved tracks",
                    owner = mc.session?.username,
                    trackCount = likedInfo?.total ?: 0,
                    imageUrl = null,
                    uri = null,
                    isLikedSongs = true,
                )
                playlists = listOf(likedEntry) + loaded
                if (selectedPlaylist == null || force) {
                    selectedPlaylist = playlists.firstOrNull()
                    selectedTrackIndex = -1
                    trackScroll = 0f
                }
                playlistsLoading = false
                val current = selectedPlaylist
                if (current != null) {
                    loadTracksFor(current, forceReload = force)
                }
            }.onFailure {
                LOGGER.warn("[Spotify][GUI] Failed to load playlists", it)
                playlistError = it.message ?: "Unable to load playlists"
                playlists = emptyList()
                playlistsLoading = false
            }
        }
    }

    private fun loadTracksFor(playlist: SpotifyPlaylistSummary, forceReload: Boolean) {
        val cacheKey = playlist.id
        val cached = trackCache[cacheKey]
        if (cached != null && !forceReload) {
            displayedTracks = cached.tracks
            updateTrackFilters()
            return
        }
        tracksLoading = true
        tracksError = null
        displayedTracks = emptyList()
        filteredTracks = emptyList()
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken(forceRefresh = forceReload)
            if (token == null) {
                tracksError = "Missing Spotify credentials."
                tracksLoading = false
                return@launch
            }
            val result = runCatching {
                if (playlist.isLikedSongs) {
                    SpotifyIntegration.service.fetchSavedTracks(token.value, SAVED_TRACK_LIMIT, 0)
                } else {
                    SpotifyIntegration.service.fetchPlaylistTracks(token.value, playlist.id, PLAYLIST_TRACK_LIMIT, 0)
                }
            }
            result.onSuccess { page ->
                trackCache[cacheKey] = page
                displayedTracks = page.tracks
                updateTrackFilters()
            }.onFailure {
                LOGGER.warn("[Spotify][GUI] Failed to load tracks", it)
                tracksError = it.message ?: "Unable to load tracks"
                displayedTracks = emptyList()
                filteredTracks = emptyList()
            }
            tracksLoading = false
        }
    }

    private fun togglePlayback() {
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val playing = playbackState?.isPlaying == true
            val result = runCatching {
                if (playing) {
                    SpotifyIntegration.service.pausePlayback(token.value)
                } else {
                    val playlist = selectedPlaylist
                    val selectedTrack = filteredTracks.getOrNull(selectedTrackIndex)
                    if (playlist != null && !playlist.uri.isNullOrBlank()) {
                        val offsetUri = selectedTrack?.let { buildTrackUri(it.id) }
                        SpotifyIntegration.service.startPlayback(token.value, contextUri = playlist.uri, offsetUri = offsetUri)
                    } else if (selectedTrack != null) {
                        SpotifyIntegration.service.startPlayback(token.value, trackUri = buildTrackUri(selectedTrack.id))
                    } else {
                        SpotifyIntegration.service.startPlayback(token.value)
                    }
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner(if (playing) "Paused playback" else "Started playback")
            }.onFailure {
                showBanner(it.message ?: "Failed to control playback")
            }
        }
    }

    private fun skipTrack(previous: Boolean) {
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val result = runCatching {
                if (previous) {
                    SpotifyIntegration.service.skipToPrevious(token.value)
                } else {
                    SpotifyIntegration.service.skipToNext(token.value)
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner(if (previous) "Previous track" else "Next track")
            }.onFailure {
                showBanner(it.message ?: "Failed to change track")
            }
        }
    }

    private fun playTrack(track: SpotifyTrack) {
        selectedTrackIndex = filteredTracks.indexOfFirst { it.id == track.id }
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val playlist = selectedPlaylist
            val result = runCatching {
                if (playlist != null && !playlist.uri.isNullOrBlank()) {
                    SpotifyIntegration.service.startPlayback(token.value, contextUri = playlist.uri, offsetUri = buildTrackUri(track.id))
                } else {
                    SpotifyIntegration.service.startPlayback(token.value, trackUri = buildTrackUri(track.id))
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner("Playing ${track.title}")
            }.onFailure {
                showBanner(it.message ?: "Failed to start track")
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        searchQuery = query
        updateTrackFilters()
    }

    private fun updateTrackFilters() {
        val query = searchQuery.trim().lowercase()
        val source = trackCache[selectedPlaylist?.id]?.tracks ?: displayedTracks
        filteredTracks = if (query.isBlank()) {
            source
        } else {
            source.filter { track ->
                track.title.lowercase().contains(query) || track.artists.lowercase().contains(query)
            }
        }
        if (selectedTrackIndex !in filteredTracks.indices) {
            selectedTrackIndex = -1
        }
        adjustTrackScroll(0f)
    }

    private fun resolvePlayPauseLabel(): String {
        return if (playbackState?.isPlaying == true) "Pause" else "Play"
    }

    private fun updatePlayPauseLabel() {
        if (::playPauseButton.isInitialized) {
            playPauseButton.displayString = resolvePlayPauseLabel()
        }
    }

    private fun showBanner(message: String) {
        bannerMessage = message
        bannerExpiry = System.currentTimeMillis() + 3500
    }

    private fun drawAmbientCover() {
        val texture = coverTexture ?: return
        GlStateManager.color(1f, 1f, 1f, 0.12f)
        mc.textureManager.bindTexture(texture)
        val size = 156
        Gui.drawScaledCustomSizeModalRect(width - size - 18, 12, 0f, 0f, 256, 256, size, size, 256f, 256f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun drawNowPlayingBadge() {
        val track = playbackState?.track ?: return
        val cardLeft = 28
        val cardTop = 48
        val cardRight = width / 2 + 90
        val cardBottom = cardTop + 60
        Gui.drawRect(cardLeft, cardTop, cardRight, cardBottom, 0x33000000)
        Gui.drawRect(cardLeft, cardBottom - 2, cardRight, cardBottom, 0xFF1DB954.toInt())
        drawTrackCover(track, cardLeft + 6, cardTop + 6, 44)
        mc.fontRendererObj.drawString("Now playing", cardLeft + 58, cardTop + 6, 0xFFB0B0B0.toInt())
        mc.fontRendererObj.drawString(trimToWidth(track.title, 160), cardLeft + 58, cardTop + 20, 0xFFFFFFFF.toInt())
        mc.fontRendererObj.drawString(trimToWidth(track.artists, 200), cardLeft + 58, cardTop + 32, 0xFFB0B0B0.toInt())
        mc.fontRendererObj.drawString(trimToWidth(track.album, 200), cardLeft + 58, cardTop + 44, 0xFF8F8F8F.toInt())
    }

    private fun drawSearchBackdrop() {
        if (!::searchField.isInitialized) {
            return
        }
        val left = searchField.xPosition - 6
        val top = searchField.yPosition - 4
        val boxWidth = width - 56
        val boxHeight = 18
        Gui.drawRect(left, top, left + boxWidth + 12, top + boxHeight + 8, 0x44000000)
        Gui.drawRect(left, top + boxHeight + 6, left + boxWidth + 12, top + boxHeight + 8, 0xFF1DB954.toInt())
    }

    private fun playlistArea(): PanelArea {
        val left = 20
        val top = HEADER_HEIGHT + 20
        val right = left + width / 4
        val bottom = height - 110
        return PanelArea(left, top, right, bottom)
    }

    private fun trackArea(): PanelArea {
        val playlistRight = playlistArea().right
        val left = playlistRight + 16
        val top = HEADER_HEIGHT + 20
        val right = width - 20
        val bottom = height - 110
        return PanelArea(left, top, right, bottom)
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun trimToWidth(text: String, width: Int): String {
        if (mc.fontRendererObj.getStringWidth(text) <= width) {
            return text
        }
        var trimmed = text
        while (trimmed.isNotEmpty() && mc.fontRendererObj.getStringWidth("$trimmed...") > width) {
            trimmed = trimmed.dropLast(1)
        }
        return if (trimmed.isEmpty()) text else "$trimmed..."
    }

    private fun drawPlaylistCover(playlist: SpotifyPlaylistSummary, x: Int, y: Int, size: Int) {
        drawArtwork(playlist.imageUrl, x, y, size, 0xFF202020.toInt())
        if (playlist.isLikedSongs) {
            Gui.drawRect(x, y + size - 4, x + size, y + size, 0xCC1DB954.toInt())
        }
    }

    private fun drawTrackCover(track: SpotifyTrack, x: Int, y: Int, size: Int) {
        drawArtwork(track.coverUrl, x, y, size, 0xFF1A1A1A.toInt())
        if (playbackState?.track?.id == track.id) {
            Gui.drawRect(x, y + size - 3, x + size, y + size, 0xCC1DB954.toInt())
        }
    }

    private fun drawArtwork(url: String?, x: Int, y: Int, size: Int, fallbackColor: Int) {
        if (url.isNullOrBlank()) {
            Gui.drawRect(x, y, x + size, y + size, fallbackColor)
            return
        }
        val cached = coverCache[url]
        if (cached != null) {
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.textureManager.bindTexture(cached)
            Gui.drawScaledCustomSizeModalRect(x, y, 0f, 0f, 256, 256, size, size, 256f, 256f)
            return
        }
        Gui.drawRect(x, y, x + size, y + size, fallbackColor)
        requestTexture(url)
    }

    private fun drawWrappedText(text: String, area: PanelArea, color: Int) {
        val lines = mc.fontRendererObj.listFormattedStringToWidth(text, area.width() - 12)
        var y = area.top + 10
        for (line in lines) {
            if (y > area.bottom - 8) {
                break
            }
            mc.fontRendererObj.drawString(line, area.left + 6, y, color)
            y += 10
        }
    }

    private fun buildTrackUri(id: String): String = if (id.startsWith("spotify:")) id else "spotify:track:$id"

    private fun updateCoverTexture(state: SpotifyState?) {
        val url = state?.track?.coverUrl
        if (url.isNullOrBlank()) {
            coverTexture = null
            coverUrl = null
            return
        }
        if (url == coverUrl && coverTexture != null) {
            return
        }
        coverUrl = url
        val cached = coverCache[url]
        if (cached != null) {
            coverTexture = cached
            return
        }
        requestTexture(url) { location ->
            if (coverUrl == url) {
                coverTexture = location
            }
        }
    }

    private fun requestTexture(url: String, onReady: ((ResourceLocation) -> Unit)? = null) {
        if (coverCache.containsKey(url)) {
            coverCache[url]?.let { onReady?.invoke(it) }
            return
        }
        SharedScopes.IO.launch {
            runCatching {
                HttpClient.get(url).use { response ->
                    ensureSuccess(response)
                    response.body.byteStream().use { stream ->
                        val image = javax.imageio.ImageIO.read(stream) ?: throw IOException("Cover art missing")
                        val texture = DynamicTexture(image)
                        mc.textureManager.getDynamicTextureLocation("spotify/" + UUID.randomUUID(), texture)
                    }
                }
            }.onSuccess { location ->
                mc.addScheduledTask {
                    coverCache[url] = location
                    onReady?.invoke(location)
                }
            }.onFailure {
                LOGGER.warn("[Spotify][GUI] Failed to load cover art from $url", it)
            }
        }
    }

    private fun ensureSuccess(response: Response) {
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code} while loading cover art")
        }
    }

    data class PanelArea(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        fun width(): Int = right - left
        fun height(): Int = bottom - top
        fun contains(x: Int, y: Int): Boolean = x in left..right && y in top..bottom
    }

    companion object {
        private const val BUTTON_BACK = 600
        private const val BUTTON_REFRESH = 601
        private const val BUTTON_PLAY_PAUSE = 602
        private const val BUTTON_PREVIOUS = 603
        private const val BUTTON_NEXT = 604
        private const val LIKED_SONGS_ID = "liked_songs"
        private const val PLAYLIST_TRACK_LIMIT = 100
        private const val SAVED_TRACK_LIMIT = 50
        private const val PLAYLIST_ROW_HEIGHT = 32f
        private const val TRACK_ROW_HEIGHT = 22f
        private const val HEADER_HEIGHT = 132
    }
}