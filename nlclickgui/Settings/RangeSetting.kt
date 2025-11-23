package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.drawRoundedRect
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.isHovering
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawCircle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.util.MathHelper
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RangeSetting(setting: Value<*>, moduleRender: net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule) :
    Downward<Value<*>>(setting, moduleRender) {

    private var draggingLeft = false
    private var draggingRight = false

    override fun draw(mouseX: Int, mouseY: Int) {
        val gui = NeverloseGui.getInstance()
        val mainx = gui.x
        val mainy = gui.y
        val rangeY = (y + getScrollY()).toInt()

        val barX = mainx + 170 + x
        val barY = (mainy + rangeY + 58).toFloat()

        val (minimum, maximum, currentStart, currentEnd) = when (setting) {
            is IntRangeValue -> Quadruple(
                setting.minimum.toDouble(),
                setting.maximum.toDouble(),
                setting.get().first.toDouble(),
                setting.get().last.toDouble()
            )

            is FloatRangeValue -> Quadruple(
                setting.minimum.toDouble(),
                setting.maximum.toDouble(),
                setting.get().start.toDouble(),
                setting.get().endInclusive.toDouble()
            )

            else -> return
        }

        val percentStart = ((currentStart - minimum) / (maximum - minimum)).coerceIn(0.0, 1.0)
        val percentEnd = ((currentEnd - minimum) / (maximum - minimum)).coerceIn(0.0, 1.0)

        val startX = barX + (60 * percentStart).toFloat()
        val endX = barX + (60 * percentEnd).toFloat()

        Fonts.Nl_16.drawString(
            setting.name,
            (mainx + 100 + x).toFloat(),
            (mainy + rangeY + 57).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )

        drawRound(barX.toFloat(), barY, 60f, 2f, 2f, if (gui.light) Color(230, 230, 230) else Color(5, 22, 41))

        val fillStart = min(startX, endX)
        val fillWidth = abs(endX - startX)
        drawRound(fillStart, barY, max(2f, fillWidth), 2f, 2f, NeverloseGui.neverlosecolor)

        drawCircle(startX, barY - 2, 5.5f, NeverloseGui.neverlosecolor)
        drawCircle(endX, barY - 2, 5.5f, NeverloseGui.neverlosecolor)

        if (draggingLeft || draggingRight) {
            val percent = ((mouseX.toFloat() - barX) / 60f).coerceIn(0f, 1f)
            val newValue = minimum + (maximum - minimum) * percent

            when (setting) {
                is IntRangeValue -> {
                    if (draggingLeft) setting.setFirst(MathHelper.floor_double(newValue).coerceAtMost(setting.get().last), true)
                    if (draggingRight) setting.setLast(MathHelper.floor_double(newValue).coerceAtLeast(setting.get().first), true)
                }

                is FloatRangeValue -> {
                    if (draggingLeft) setting.setFirst(newValue.toFloat().coerceAtMost(setting.get().endInclusive), true)
                    if (draggingRight) setting.setLast(newValue.toFloat().coerceAtLeast(setting.get().start), true)
                }
            }
        }

        val valueString = when (setting) {
            is IntRangeValue -> "${setting.get().first} - ${setting.get().last}${setting.suffix ?: ""}"
            is FloatRangeValue -> "${"%.2f".format(setting.get().start)} - ${"%.2f".format(setting.get().endInclusive)}${setting.suffix ?: ""}"
            else -> ""
        }

        val stringWidth = Fonts.Nl_15.stringWidth(valueString) + 4
        drawRoundedRect(
            mainx + 235 + x,
            (mainy + rangeY + 55).toFloat(),
            stringWidth.toFloat(),
            9f,
            1f,
            if (gui.light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString(
            valueString,
            mainx + 237 + x,
            (mainy + rangeY + 58).toFloat(),
            if (gui.light) Color(95, 95, 95).rgb else -1
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val gui = NeverloseGui.getInstance()
        val barX = gui.x + 170 + x
        val barY = gui.y + (y + getScrollY()).toInt() + 58

        val percentStart: Double
        val percentEnd: Double
        when (setting) {
            is IntRangeValue -> {
                percentStart = (setting.get().first - setting.minimum).toDouble() / (setting.maximum - setting.minimum)
                percentEnd = (setting.get().last - setting.minimum).toDouble() / (setting.maximum - setting.minimum)
            }

            is FloatRangeValue -> {
                percentStart = (setting.get().start - setting.minimum) / (setting.maximum - setting.minimum)
                percentEnd = (setting.get().endInclusive - setting.minimum) / (setting.maximum - setting.minimum)
            }

            else -> return
        }

        val startX = barX + 60 * percentStart
        val endX = barX + 60 * percentEnd

        if (mouseButton == 0) {
            val nearStart = abs(mouseX - startX) <= 6
            val nearEnd = abs(mouseX - endX) <= 6

            if (nearStart || nearEnd || isHovering(barX.toFloat(), barY.toFloat(), 60f, 6f, mouseX, mouseY)) {
                if (nearStart && nearEnd) {
                    if (abs(mouseX - startX) <= abs(mouseX - endX)) draggingLeft = true else draggingRight = true
                } else if (nearStart) {
                    draggingLeft = true
                } else if (nearEnd) {
                    draggingRight = true
                } else {
                    if (abs(mouseX - startX) < abs(mouseX - endX)) draggingLeft = true else draggingRight = true
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) {
            draggingLeft = false
            draggingRight = false
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
