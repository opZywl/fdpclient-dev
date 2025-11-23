package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class Numbersetting(setting: Value<*>, moduleRender: NlModule) : Downward<Value<*>>(setting, moduleRender) {
    var percent = 0f
    private var iloveyou = false
    private var isset = false
    private var finalvalue: String? = null
    var hoveringAnimation: Animation = DecelerateAnimation(225.0, 1.0, Direction.BACKWARDS)

    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = NeverloseGui.getInstance().x
        val mainy = NeverloseGui.getInstance().y
        val numbery = (y + scrollY).toInt()
        hoveringAnimation.direction = if (iloveyou || RenderUtil.isHovering((NeverloseGui.getInstance().x + 170 + x).toFloat(), (NeverloseGui.getInstance().y + (y + scrollY).toInt() + 58).toFloat(), 60f, 2f, mouseX, mouseY)) Direction.FORWARDS else Direction.BACKWARDS
        val clamp = MathHelper.clamp_double(Minecraft.getDebugFPS() / 30.0, 1.0, 9999.0)
        var minimum = 0.0
        var maximum = 1.0
        if (setting is IntValue) {
            minimum = setting.minimum.toDouble()
            maximum = setting.maximum.toDouble()
        } else if (setting is FloatValue) {
            minimum = setting.minimum.toDouble()
            maximum = setting.maximum.toDouble()
        }
        val current = (setting.get() as Number).toDouble()
        val percentBar = (current - minimum) / (maximum - minimum)
        percent = maxOf(0f, minOf(1f, (percent + (maxOf(0.0, minOf(percentBar, 1.0)) - percent) * (0.2 / clamp)).toFloat()))
        Fonts.Nl_16.drawString(setting.name, (mainx + 100 + x).toFloat(), (mainy + numbery + 57).toFloat(), if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1)
        RoundedUtil.drawRound((mainx + 170 + x).toFloat(), (mainy + numbery + 58).toFloat(), 60f, 2f, 2f, if (NeverloseGui.getInstance().light) Color(230, 230, 230) else Color(5, 22, 41))
        RoundedUtil.drawRound((mainx + 170 + x).toFloat(), (mainy + numbery + 58).toFloat(), 60 * percent, 2f, 2f, Color(12, 100, 138))
        RoundedUtil.drawCircle(mainx + 167 + x + 60 * percent, (mainy + numbery + 56).toFloat(), (5.5f + (0.5f * hoveringAnimation.output)).toFloat(), NeverloseGui.neverlosecolor)
        if (iloveyou) {
            val percentt = minOf(1f, maxOf(0f, ((mouseX - (mainx + 170 + x)) / 99.0f) * 1.55f))
            val newValue = percentt * (maximum - minimum) + minimum
            if (setting is IntValue) {
                setting.set(newValue.toInt(), true)
            } else if (setting is FloatValue) {
                setting.set(newValue.toFloat(), true)
            }
        }
        if (isset) {
            GL11.glTranslatef(0f, 0f, 2f)
        }
        val stringWidth = Fonts.Nl_15.stringWidth(if (isset) "$finalvalue_" else "$current") + 4
        RenderUtil.drawRoundedRect((mainx + 235 + x).toFloat(), (mainy + numbery + 55).toFloat(), stringWidth.toFloat(), 9f, 1, if (NeverloseGui.getInstance().light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb, 1, Color(13, 24, 35).rgb)
        Fonts.Nl_15.drawString(if (isset) "$finalvalue_" else "$current", (mainx + 237 + x).toFloat(), (mainy + numbery + 58).toFloat(), if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1)
        if (isset) {
            GL11.glTranslatef(0f, 0f, -2f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val current = (setting.get() as Number).toDouble()
        if (RenderUtil.isHovering((NeverloseGui.getInstance().x + 170 + x).toFloat(), (NeverloseGui.getInstance().y + (y + scrollY).toInt() + 58).toFloat(), 60f, 2f, mouseX, mouseY) && !isset) {
            if (mouseButton == 0) {
                iloveyou = true
            }
        }
        val stringWidth = Fonts.Nl_15.stringWidth(if (isset) "$finalvalue_" else "$current") + 4
        if (RenderUtil.isHovering((NeverloseGui.getInstance().x + 235 + x).toFloat(), (NeverloseGui.getInstance().y + (y + scrollY) + 55).toFloat(), stringWidth.toFloat(), 9f, mouseX, mouseY)) {
            if (mouseButton == 0) {
                finalvalue = current.toString()
                isset = true
            }
        } else {
            if (mouseButton == 0) {
                isset = false
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) iloveyou = false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (isset) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE -> isset = false
                Keyboard.KEY_PERIOD -> if (!finalvalue.orEmpty().contains(".")) {
                    finalvalue += typedChar
                }
                Keyboard.KEY_BACK -> if (!finalvalue.isNullOrEmpty()) {
                    finalvalue = finalvalue!!.substring(0, finalvalue!!.length - 1)
                }
                Keyboard.KEY_RETURN -> {
                    try {
                        if (setting is FloatValue) {
                            val floatSetting = setting
                            val value = finalvalue?.toFloat() ?: floatSetting.get()
                            val max = floatSetting.maximum
                            val min = floatSetting.minimum
                            floatSetting.set(value.coerceIn(min, max), true)
                        } else if (setting is IntValue) {
                            val intSetting = setting
                            val value = finalvalue?.toInt() ?: intSetting.get()
                            val max = intSetting.maximum
                            val min = intSetting.minimum
                            intSetting.set(value.coerceIn(min, max), true)
                        }
                    } catch (_: NumberFormatException) {
                    }
                    isset = false
                }
                else -> if (keynumbers(keyCode)) {
                    finalvalue += typedChar
                }
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun keynumbers(keyCode: Int): Boolean {
        return keyCode == Keyboard.KEY_0 || keyCode == Keyboard.KEY_1 || keyCode == Keyboard.KEY_2 || keyCode == Keyboard.KEY_3 || keyCode == Keyboard.KEY_4 || keyCode == Keyboard.KEY_6 || keyCode == Keyboard.KEY_5 || keyCode == Keyboard.KEY_7 || keyCode == Keyboard.KEY_8 || keyCode == Keyboard.KEY_9 || keyCode == Keyboard.KEY_PERIOD || keyCode == Keyboard.KEY_MINUS
    }
}
