package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.MathUtil

class NlSub(val subCategory: Category.SubCategory, val y2: Int) {

    var x = 0
    var y = 0
    var w = 0
    var h = 0

    val nlModules: MutableList<NlModule> = ArrayList()

    var alphaani: Animation = EaseInOutQuad(150.0, 1.0, Direction.BACKWARDS)

    private var maxScroll = Float.MAX_VALUE
    private var minScroll = 0f
    private var rawScroll = 0f

    private var scroll = 0f

    private var scrollAnimation: SmoothStepAnimation = SmoothStepAnimation(0.0, 0.0, Direction.BACKWARDS)

    init {
        var count = 0

        for (holder in FDPClient.moduleManager) {
            if (holder.subCategory == subCategory) {
                nlModules.add(NlModule(this, holder, count % 2 == 0))
                count++
            }
        }
    }

    fun draw(mx: Int, my: Int) {
        alphaani.setDirection(if (isSelected()) Direction.FORWARDS else Direction.BACKWARDS)

        if (isSelected()) {
            RoundedUtil.drawRound(
                x + 7,
                y + y2 + 8,
                76,
                15,
                2f,
                if (NeverloseGui.getInstance().light) Color(200, 200, 200, (100 + (155 * alphaani.output)).toInt()) else Color(
                    8,
                    48,
                    70,
                    (100 + (155 * alphaani.output)).toInt()
                )
            )
        }

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString(subCategory.icon, (x + 10).toFloat(), (y + y2 + 14).toFloat(), NeverloseGui.neverlosecolor.rgb)

        Fonts.Nl.Nl_18.getNl_18().drawString(
            subCategory.toString(),
            x + 10 + Fonts.NlIcon.nlfont_20.getNlfont_20().stringWidth(subCategory.icon) + 8,
            y + y2 + 13,
            if (NeverloseGui.getInstance().light) Color(18, 18, 19).rgb else -1
        )

        if (isSelected() && subCategory != Category.SubCategory.CONFIGS) {
            val scrolll = scroll
            for (nlModule in nlModules) {
                nlModule.scrollY = MathUtil.roundToHalf(scrolll).toInt()
            }
            onScroll(40)

            maxScroll = if (nlModules.isNotEmpty()) {
                Math.max(0.0, (nlModules[nlModules.size - 1].y + 50 + nlModules[nlModules.size - 1].posy + nlModules[nlModules.size - 1].getHeight()).toDouble()).toFloat()
            } else {
                0f
            }

            for (nlModule in nlModules) {
                nlModule.x = x
                nlModule.y = y
                nlModule.w = w
                nlModule.h = h

                GL11.glEnable(GL11.GL_SCISSOR_TEST)
                RenderUtil.scissor(x + 90, y + 40, w - 90, h - 40)

                nlModule.draw(mx, my)
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
            }
        }

        if (isSelected() && subCategory == Category.SubCategory.CONFIGS) {
            val scrolll = scroll

            NeverloseGui.getInstance().configs.scy = MathUtil.roundToHalf(scrolll).toInt()
            onScroll(40)
            maxScroll = Math.max(0, NeverloseGui.getInstance().configs.yY).toFloat()

            var x2 = 0
            var i = 0
            for (type in Category.values()) {
                if (type.name.equals("World", ignoreCase = true) ||
                    type.name.equals("Interface", ignoreCase = true) ||
                    type.name.equals("Config", ignoreCase = true)
                ) continue

                var l = ""
                l = when {
                    type.name.equals("Combat", ignoreCase = true) -> "D"
                    type.name.equals("Movement", ignoreCase = true) -> "A"
                    type.name.equals("Player", ignoreCase = true) -> "B"
                    type.name.equals("Render", ignoreCase = true) -> "C"
                    else -> l
                }

                RoundedUtil.drawRoundOutline(
                    x + 170 + x2,
                    NeverloseGui.getInstance().y + 13,
                    15,
                    15,
                    1f,
                    0.1f,
                    if (NeverloseGui.getInstance().configs.loads[i]) Color(10, 122, 182) else Color(15, 15, 19),
                    if (NeverloseGui.getInstance().configs.loads[i]) Color(10, 122, 182) else Color(22, 22, 24)
                )

                Fonts.ICONFONT_17.drawString(l, x + 173.5f + x2, NeverloseGui.getInstance().y + 19f, -1)

                x2 += 20
                i++
            }

            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            RenderUtil.scissor(x + 90, y + 40, w - 90, h - 40)
            NeverloseGui.getInstance().configs.posx = x
            NeverloseGui.getInstance().configs.posy = y
            NeverloseGui.getInstance().configs.draw(mx, my)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    fun onScroll(ms: Int) {
        scroll = (rawScroll - scrollAnimation.output).toFloat()
        rawScroll += Mouse.getDWheel() / 4f
        rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll)
        scrollAnimation = SmoothStepAnimation(ms.toDouble(), (rawScroll - scroll).toDouble(), Direction.BACKWARDS)
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        nlModules.forEach { it.keyTyped(typedChar, keyCode) }
    }

    fun released(mx: Int, my: Int, mb: Int) {
        nlModules.forEach { it.released(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (isSelected()) nlModules.forEach { it.click(mx, my, mb) }

        if (isSelected() && subCategory == Category.SubCategory.CONFIGS) {
            var x2 = 0
            var i = 0
            for (type in Category.values()) {
                if (type.name.equals("World", ignoreCase = true) ||
                    type.name.equals("Interface", ignoreCase = true) ||
                    type.name.equals("Config", ignoreCase = true)
                ) continue

                if (RenderUtil.isHovering(x + 170 + x2, NeverloseGui.getInstance().y + 13, 15, 15, mx, my)) {
                    NeverloseGui.getInstance().configs.loads[i] = !NeverloseGui.getInstance().configs.loads[i]
                }

                x2 += 20
                i++
            }

            NeverloseGui.getInstance().configs.click(mx, my, mb)
        }
    }

    fun isSelected(): Boolean {
        return NeverloseGui.getInstance().subCategory == subCategory
    }
}
