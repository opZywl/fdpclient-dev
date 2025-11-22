package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.ColorSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.StringsSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import java.awt.Color

class NlModule(val nlSub: NlSub, val module: Module, private val lef: Boolean) {

    var x = 0
    var y = 0
    var w = 0
    var h = 0

    var posx = 0
    var posy = 0

    var height = 0

    val downwards: MutableList<Downward> = ArrayList()

    var scrollY = 0

    var toggleAnimation = DecelerateAnimation(225.0, 1.0, Direction.BACKWARDS)

    var hoveringAnimation = DecelerateAnimation(225.0, 1.0, Direction.BACKWARDS)

    init {
        posx = if (lef) 0 else 170

        for (setting in module.values) {
            when (setting) {
                is BoolValue -> downwards.add(BoolSetting(setting, this))
                is FloatValue, is IntValue -> downwards.add(Numbersetting(setting, this))
                is ListValue -> downwards.add(StringsSetting(setting, this))
                is ColorValue -> downwards.add(ColorSetting(setting, this))
            }
        }
    }

    fun getHeight(): Int {
        var h = 20
        for (s in module.values.filter { it.shouldRender() }) {
            h += 20
        }
        if (module.values.isEmpty()) {
            h += 20
        }
        return h
    }

    fun getY(): Int {
        var leftAdd = 0
        var rightAdd = 0

        for (tabModule in nlSub.nlModules) {
            if (tabModule == this) {
                break
            } else {
                if (tabModule.lef) {
                    leftAdd += tabModule.getHeight() + 10
                } else {
                    rightAdd += tabModule.getHeight() + 10
                }
            }
        }

        return if (lef) leftAdd else rightAdd
    }

    fun draw(mx: Int, my: Int) {
        posy = getY()

        RoundedUtil.drawRound(
            x + 95 + posx,
            y + 50 + posy + scrollY,
            160,
            getHeight(),
            2f,
            if (NeverloseGui.getInstance().light) Color(245, 245, 245) else Color(3, 13, 26)
        )

        Fonts.Nl.Nl_18.getNl_18().drawString(
            module.name,
            x + 100 + posx,
            y + posy + 55 + scrollY,
            if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
        )

        RoundedUtil.drawRound(
            x + 100 + posx,
            y + 65 + posy + scrollY,
            150,
            0.7f,
            0f,
            if (NeverloseGui.getInstance().light) Color(213, 213, 213) else Color(9, 21, 34)
        )

        hoveringAnimation.setDirection(if (RenderUtil.isHovering(x + 265 - 32 + posx, y + posy + scrollY + 56, 16, 4.5f, mx, my)) Direction.FORWARDS else Direction.BACKWARDS)

        var cheigt = 20
        for (downward in downwards.filter { it.setting.shouldRender() }) {
            downward.x = posx
            downward.y = getY() + cheigt
            cheigt += 20

            downward.draw(mx, my)
        }

        rendertoggle()

        if (module.values.isEmpty()) {
            Fonts.Nl.Nl_22.getNl_22().drawString(
                "No Settings.",
                x + 100 + posx,
                y + posy + scrollY + 72,
                if (NeverloseGui.getInstance().light) Color(95, 95, 95).rgb else -1
            )
        }
    }

    private fun rendertoggle() {
        val darkRectColor = Color(29, 29, 39, 255)

        val darkRectHover = RenderUtil.brighter(darkRectColor, .8f)

        val accentCircle = RenderUtil.darker(NeverloseGui.neverlosecolor, .5f)

        toggleAnimation.setDirection(if (module.state) Direction.FORWARDS else Direction.BACKWARDS)

        RoundedUtil.drawRound(
            x + 265 - 32 + posx,
            y + posy + scrollY + 56,
            16,
            4.5f,
            2f,
            RenderUtil.interpolateColorC(RenderUtil.applyOpacity(darkRectHover, .5f), accentCircle, toggleAnimation.output.toFloat())
        )

        RenderUtil.fakeCircleGlow(
            (x + 265 + 3 - 32 + posx + 11 * toggleAnimation.output).toFloat(),
            (y + posy + scrollY + 56 + 2).toFloat(),
            6,
            Color.BLACK,
            .3f
        )

        RenderUtil.resetColor()

        RoundedUtil.drawRound(
            (x + 265 - 32 + posx + 11 * toggleAnimation.output).toFloat(),
            (y + posy + scrollY + 55).toFloat(),
            6.5f,
            6.5f,
            3f,
            if (module.state) NeverloseGui.neverlosecolor else if (NeverloseGui.getInstance().light) Color(255, 255, 255) else Color(
                (68 - (28 * hoveringAnimation.output)).toInt(),
                (82 + (44 * hoveringAnimation.output)).toInt(),
                (87 + (83 * hoveringAnimation.output)).toInt()
            )
        )
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        downwards.forEach { it.keyTyped(typedChar, keyCode) }
    }

    fun released(mx: Int, my: Int, mb: Int) {
        downwards.filter { it.setting.shouldRender() }.forEach { it.mouseReleased(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        downwards.filter { it.setting.shouldRender() }.forEach { it.mouseClicked(mx, my, mb) }

        if (RenderUtil.isHovering(x + 265 - 32 + posx, y + posy + scrollY + 56, 16, 4.5f, mx, my) && mb == 0) {
            module.toggle()
        }
    }
}
