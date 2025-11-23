package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.drawRoundedRect
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.isHovering
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color

class FontSetting(setting: FontValue, moduleRender: net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule) :
    Downward<FontValue>(setting, moduleRender) {

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val fontY = (y + getScrollY()).toInt()

        Fonts.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x).toFloat(),
            (mainy + fontY + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        val display = setting.displayName
        val rectWidth = maxOf(100, Fonts.Nl_15.stringWidth(display) + 20)

        val rectX = mainx + 170 + x
        val rectY = mainy + fontY + 54

        drawRoundedRect(
            rectX,
            rectY.toFloat(),
            rectWidth.toFloat(),
            14f,
            2f,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString("<", rectX + 4, (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)
        Fonts.Nl_15.drawString(">", rectX + rectWidth - 9, (rectY + 5).toFloat(), if (gui.light) Color(95, 95, 95).rgb else -1)

        Fonts.Nl_15.drawCenteredString(
            display,
            (rectX + rectWidth / 2f),
            (rectY + 5).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val rectX = gui.x + 170 + x
        val rectY = gui.y + (y + getScrollY()).toInt() + 54
        val display = setting.displayName
        val rectWidth = maxOf(100, Fonts.Nl_15.stringWidth(display) + 20)

        if (mouseButton == 0 && isHovering(rectX.toFloat(), rectY.toFloat(), rectWidth.toFloat(), 14f, mouseX, mouseY)) {
            val relativeX = mouseX - rectX
            when {
                relativeX < 20 -> setting.previous()
                relativeX > rectWidth - 20 -> setting.next()
                else -> setting.next()
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
}
