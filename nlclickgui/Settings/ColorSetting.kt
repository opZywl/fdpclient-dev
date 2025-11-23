package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

class ColorSetting(setting: ColorValue, moduleRender: NlModule) : Downward<ColorValue>(setting, moduleRender) {

    private var dragging: ColorValue.SliderType? = null

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val colory = (y + getScrollY()).toInt()

        val titleColor = if (gui.light) Color(95, 95, 95).rgb else -1
        Fonts.Nl_16.drawString(setting.name, (mainx + 100 + x).toFloat(), (mainy + colory + 57).toFloat(), titleColor)

        val currentColor = setting.selectedColor()
        val previewX = (mainx + 170 + x).toFloat()
        val previewY = (mainy + colory + 52).toFloat()

        RoundedUtil.drawRound(previewX, previewY, 18f, 12f, 2f, currentColor)
        RenderUtil.drawBorderedRect(previewX, previewY, previewX + 18, previewY + 12, 1f, Color(0, 0, 0, 60).rgb, Color(0, 0, 0, 80).rgb)

        val rainbowPreviewX = previewX + 24
        val rainbowColor = Color.getHSBColor((System.currentTimeMillis() % 2500L) / 2500f, 1f, 1f)
        RoundedUtil.drawRound(rainbowPreviewX, previewY, 18f, 12f, 2f, if (setting.rainbow) rainbowColor else Color(50, 50, 50, 90))
        RenderUtil.drawBorderedRect(rainbowPreviewX, previewY, rainbowPreviewX + 18, previewY + 12, 1f, Color(0, 0, 0, 60).rgb, Color(0, 0, 0, 80).rgb)

        Fonts.Nl_15.drawString("#%08X".format(currentColor.rgb), previewX + 46, previewY + 3, titleColor)

        if (setting.showPicker && dragging != null) {
            updateFromMouse(mouseX, mouseY, dragging!!)
        }

        if (setting.showPicker) {
            GL11.glTranslatef(0f, 0f, 2f)
            drawPicker(mouseX, mouseY, previewX, previewY + 20)
            GL11.glTranslatef(0f, 0f, -2f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val previewX = (gui.x + 170 + x).toFloat()
        val previewY = (gui.y + (y + getScrollY()).toInt() + 52).toFloat()

        val inColorPreview = RenderUtil.isHovering(previewX, previewY, 18f, 12f, mouseX, mouseY)
        val inRainbowPreview = RenderUtil.isHovering(previewX + 24, previewY, 18f, 12f, mouseX, mouseY)

        if (mouseButton == 0 && inColorPreview) {
            setting.showPicker = !setting.showPicker
            if (setting.rainbow) setting.rainbow = false
        }
        if (mouseButton == 1 && inColorPreview) {
            setting.rainbow = !setting.rainbow
        }
        if (mouseButton == 0 && inRainbowPreview) {
            setting.rainbow = !setting.rainbow
        }
        if (mouseButton == 0 && setting.showPicker) {
            val pickerTop = previewY + 20
            val slider = detectSlider(mouseX, mouseY, previewX, pickerTop)
            if (slider != null) {
                dragging = slider
                updateFromMouse(mouseX, mouseY, slider)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) dragging = null
    }

    private fun drawPicker(mouseX: Int, mouseY: Int, baseX: Float, baseY: Float) {
        val gui = NeverloseGui.getInstance()
        val bgColor = if (gui.light) Color(255, 255, 255) else Color(0, 5, 19)
        val outline = Color(13, 24, 35)

        val padding = 4f
        val pickerWidth = 108f
        val pickerHeight = 84f
        RenderUtil.drawRoundedRect(baseX, baseY, pickerWidth, pickerHeight, 3f, bgColor.rgb, 1f, outline.rgb)

        val squareSize = 70f
        val squareX = baseX + padding
        val squareY = baseY + padding

        val hueColor = Color(Color.HSBtoRGB(setting.hueSliderY, 1f, 1f))
        drawGradientRect(squareX.toInt(), squareY.toInt(), (squareX + squareSize).toInt(), (squareY + squareSize).toInt(), Color.WHITE.rgb, hueColor.rgb)
        drawGradientRect(squareX.toInt(), squareY.toInt(), (squareX + squareSize).toInt(), (squareY + squareSize).toInt(), Color(0, 0, 0, 0).rgb, Color(0, 0, 0, 255).rgb)

        val markerX = squareX + setting.colorPickerPos.x.coerceIn(0f, 1f) * squareSize
        val markerY = squareY + setting.colorPickerPos.y.coerceIn(0f, 1f) * squareSize
        RoundedUtil.drawCircle(markerX, markerY, 2.5f, Color.WHITE)

        val sliderHeight = squareSize
        val hueX = squareX + squareSize + padding
        for (i in 0 until 6) {
            val startHue = i / 6f
            val endHue = (i + 1) / 6f
            val startColor = Color.getHSBColor(startHue, 1f, 1f)
            val endColor = Color.getHSBColor(endHue, 1f, 1f)
            val startY = squareY + sliderHeight * (i / 6f)
            val endY = squareY + sliderHeight * ((i + 1) / 6f)
            drawGradientRect(hueX.toInt(), startY.toInt(), (hueX + 8).toInt(), endY.toInt(), startColor.rgb, endColor.rgb)
        }
        val hueMarkerY = squareY + setting.hueSliderY.coerceIn(0f, 1f) * sliderHeight
        RenderUtil.drawBorderedRect(hueX - 1, hueMarkerY - 1, hueX + 9, hueMarkerY + 1, 1f, Color.WHITE.rgb, Color(0, 0, 0, 120).rgb)

        val opacityX = hueX + 12
        val opaqueColor = currentBaseColor().let { Color(it.red, it.green, it.blue, 255) }
        val transparentColor = Color(opaqueColor.red, opaqueColor.green, opaqueColor.blue, 0)
        drawGradientRect(opacityX.toInt(), squareY.toInt(), (opacityX + 8).toInt(), (squareY + sliderHeight).toInt(), opaqueColor.rgb, transparentColor.rgb)
        val opacityMarkerY = squareY + (1 - setting.opacitySliderY.coerceIn(0f, 1f)) * sliderHeight
        RenderUtil.drawBorderedRect(opacityX - 1, opacityMarkerY - 1, opacityX + 9, opacityMarkerY + 1, 1f, Color.WHITE.rgb, Color(0, 0, 0, 120).rgb)

        val slider = detectSlider(mouseX, mouseY, baseX, baseY)
        if (slider != null && dragging == null && RenderUtil.isHovering(squareX, squareY, pickerWidth - padding * 2, sliderHeight, mouseX, mouseY)) {
            Fonts.Nl_12.drawString(slider.name.lowercase().replaceFirstChar { it.titlecase() }, squareX, baseY + pickerHeight - 10, titleColor(gui))
        }
    }

    private fun detectSlider(mouseX: Int, mouseY: Int, baseX: Float, baseY: Float): ColorValue.SliderType? {
        val padding = 4f
        val squareSize = 70f
        val squareX = baseX + padding
        val squareY = baseY + padding
        val hueX = squareX + squareSize + padding
        val opacityX = hueX + 12

        return when {
            RenderUtil.isHovering(squareX, squareY, squareSize, squareSize, mouseX, mouseY) -> ColorValue.SliderType.COLOR
            RenderUtil.isHovering(hueX, squareY, 8f, squareSize, mouseX, mouseY) -> ColorValue.SliderType.HUE
            RenderUtil.isHovering(opacityX, squareY, 8f, squareSize, mouseX, mouseY) -> ColorValue.SliderType.OPACITY
            else -> null
        }
    }

    private fun updateFromMouse(mouseX: Int, mouseY: Int, slider: ColorValue.SliderType) {
        val gui = NeverloseGui.getInstance()
        val baseX = (gui.x + 170 + x).toFloat()
        val baseY = (gui.y + (y + getScrollY()).toInt() + 72).toFloat()
        val padding = 4f
        val squareSize = 70f
        val squareX = baseX + padding
        val squareY = baseY + padding

        when (slider) {
            ColorValue.SliderType.COLOR -> {
                val newS = ((mouseX - squareX) / squareSize).coerceIn(0f, 1f)
                val newB = (1 - ((mouseY - squareY) / squareSize)).coerceIn(0f, 1f)
                setting.colorPickerPos.x = newS
                setting.colorPickerPos.y = 1 - newB
            }

            ColorValue.SliderType.HUE -> {
                val newHue = ((mouseY - squareY) / squareSize).coerceIn(0f, 1f)
                setting.hueSliderY = newHue
            }

            ColorValue.SliderType.OPACITY -> {
                val newOpacity = (1 - ((mouseY - squareY) / squareSize)).coerceIn(0f, 1f)
                setting.opacitySliderY = newOpacity
            }
        }

        val baseColor = Color(Color.HSBtoRGB(setting.hueSliderY, setting.colorPickerPos.x, 1 - setting.colorPickerPos.y))
        val finalColor = Color(baseColor.red, baseColor.green, baseColor.blue, (setting.opacitySliderY * 255).roundToInt())
        setting.rainbow = false
        setting.set(finalColor, true)
    }

    private fun currentBaseColor(): Color {
        return Color(Color.HSBtoRGB(setting.hueSliderY, setting.colorPickerPos.x, 1 - setting.colorPickerPos.y))
    }

    private fun titleColor(gui: NeverloseGui): Int {
        return if (gui.light) Color(95, 95, 95).rgb else -1
    }
}
