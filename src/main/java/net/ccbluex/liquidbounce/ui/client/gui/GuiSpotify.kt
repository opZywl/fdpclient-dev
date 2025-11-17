/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraftforge.fml.client.config.GuiSlider
import org.lwjgl.input.Keyboard
import java.awt.Desktop
import java.net.URI
import kotlin.math.max

class GuiSpotify(private val previousScreen: GuiScreen?) : AbstractScreen() {

    private lateinit var clientIdField: GuiTextField
    private lateinit var clientSecretField: GuiTextField
    private lateinit var refreshTokenField: GuiTextField
    private lateinit var reconnectButton: GuiButton
    private lateinit var pollSlider: GuiSlider

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        buttonList.clear()
        textFields.clear()

        val fieldWidth = 260
        val startX = width / 2 - fieldWidth / 2
        var currentY = height / 4

        clientIdField = textField(0, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 128
            text = SpotifyModule.clientId
        }
        currentY += 26

        clientSecretField = textField(1, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 128
            text = SpotifyModule.clientSecret
        }
        currentY += 26

        refreshTokenField = textField(2, Fonts.fontSemibold35, startX, currentY, fieldWidth, 20) {
            maxStringLength = 256
            text = SpotifyModule.refreshToken
        }
        currentY += 32

        pollSlider = GuiSlider(
            3,
            startX,
            currentY,
            fieldWidth,
            20,
            "Poll interval (",
            "s)",
            3.0,
            60.0,
            SpotifyModule.pollIntervalSeconds.toDouble(),
            false,
            true,
        ) { slider ->
            SpotifyModule.setPollInterval(slider.valueInt)
        }
        +pollSlider
        currentY += 26

        reconnectButton = +GuiButton(4, startX, currentY, fieldWidth, 20, reconnectLabel())
        currentY += 24

        +GuiButton(5, startX, currentY, fieldWidth, 20, "Save credentials")
        currentY += 24

        +GuiButton(6, startX, currentY, fieldWidth, 20, "Open Spotify Dashboard")
        currentY += 24

        +GuiButton(7, startX, currentY, fieldWidth, 20, "Back")
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            4 -> {
                SpotifyModule.toggleAutoReconnect()
                button.displayString = reconnectLabel()
            }

            5 -> {
                SpotifyModule.updateCredentials(
                    clientIdField.text.trim(),
                    clientSecretField.text.trim(),
                    refreshTokenField.text.trim(),
                )
                chat("§aSaved Spotify credentials.")
            }

            6 -> openDashboard()
            7 -> mc.displayGuiScreen(previousScreen)
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        Keyboard.enableRepeatEvents(false)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        val titleFont = Fonts.fontBold40
        val smallFont = Fonts.fontSemibold35
        titleFont.drawCenteredString("Spotify Integration", width / 2f, height / 4f - 40f, -1, true)

        val connectionText = "State: ${SpotifyModule.connectionState.displayName}"
        smallFont.drawCenteredString(connectionText, width / 2f, height / 4f - 20f, -1, true)

        val currentState = SpotifyModule.currentState
        drawPlaybackInfo(currentState)

        val error = SpotifyModule.lastErrorMessage
        if (!error.isNullOrBlank()) {
            smallFont.drawCenteredString("Last error: $error", width / 2f, height - 35f, 0xFF5555, true)
        }

        clientIdField.drawTextBox()
        clientSecretField.drawTextBox()
        refreshTokenField.drawTextBox()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen)
            return
        }

        if (clientIdField.textboxKeyTyped(typedChar, keyCode) ||
            clientSecretField.textboxKeyTyped(typedChar, keyCode) ||
            refreshTokenField.textboxKeyTyped(typedChar, keyCode)
        ) {
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    private fun drawPlaybackInfo(state: SpotifyState?) {
        val infoFont = Fonts.fontSemibold35
        val lines = mutableListOf<String>()
        val trackState = state
        if (trackState != null && trackState.track != null) {
            val track = trackState.track
            lines += "Track: ${track.title}"
            lines += "Artists: ${track.artists}"
            if (track.album.isNotBlank()) {
                lines += "Album: ${track.album}"
            }
            val progress = formatProgress(trackState)
            lines += "Progress: $progress"
            lines += if (trackState.isPlaying) "Status: Playing" else "Status: Paused"
        } else {
            lines += "No playback detected."
            lines += "Start Spotify on any device to see the track information."
        }

        val startY = height / 2 + 30
        lines.forEachIndexed { index, line ->
            infoFont.drawCenteredString(line, width / 2f, (startY + index * 12).toFloat(), -1, true)
        }
    }

    private fun formatProgress(state: SpotifyState): String {
        val position = max(0, state.progressMs)
        val minutes = position / 1000 / 60
        val seconds = (position / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun reconnectLabel(): String = "Auto reconnect: ${if (SpotifyModule.autoReconnect) "On" else "Off"}"

    private fun openDashboard() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI("https://developer.spotify.com/dashboard"))
            }
        } catch (ex: Exception) {
            chat("§cFailed to open browser: ${ex.message}")
        }
    }
}
