package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.toList

class NlSub(private val parentCategory: Category, var subCategory: Category.SubCategory, var y2: Int) {
    var x = 0
    var y = 0
    var w = 0
    var h = 0
    var nlModules: MutableList<NlModule> = ArrayList()
    private var visibleModules: MutableList<NlModule> = ArrayList()
    var alphaani: Animation = EaseInOutQuad(150.0, 1.0, Direction.BACKWARDS)
    private var maxScroll = Float.MAX_VALUE
    private var minScroll = 0f
    private var rawScroll = 0f
    private var scroll = 0f
    private var scrollAnimation: Animation = SmoothStepAnimation(0.0, 0.0, Direction.BACKWARDS)

    init {
        var count = 0
        for (holder in FDPClient.moduleManager) {
            if (holder.category == parentCategory && holder.subCategory == subCategory) {
                nlModules.add(NlModule(this, holder, count % 2 == 0))
                count++
            }
        }
    }

    fun draw(mx: Int, my: Int) {
        alphaani.direction = if (isSelected) Direction.FORWARDS else Direction.BACKWARDS
        if (isSelected) {
            RoundedUtil.drawRound((x + 7).toFloat(), (y + y2 + 8).toFloat(), 76f, 15f, 2f, if (NeverloseGui.getInstance().light) Color(200, 200, 200, (100 + (155 * alphaani.output)).toInt()) else Color(8, 48, 70, (100 + (155 * alphaani.output)).toInt()))
        }
        Fonts.NlIcon.nlfont_20.nlfont_20.drawString(icon, (x + 10).toFloat(), (y + y2 + 14).toFloat(), NeverloseGui.neverlosecolor.rgb)
        Fonts.Nl.Nl_18.nl_18.drawString(subCategory.toString(), (x + 10 + Fonts.NlIcon.nlfont_20.nlfont_20.stringWidth(icon) + 8).toFloat(), (y + y2 + 13).toFloat(), if (NeverloseGui.getInstance().light) Color(18, 18, 19).rgb else -1)
        if (isSelected && subCategory != Category.SubCategory.CONFIGS) {
            val scrolll = scroll
            visibleModules = getVisibleModules().toMutableList()
            for (nlModule in visibleModules) {
                nlModule.scrollY = scrolll.toInt()
            }
            onScroll(40)
            if (visibleModules.isNotEmpty()) {
                val lastModule = visibleModules[visibleModules.size - 1]
                maxScroll = max(0f, (lastModule.y + 50 + lastModule.posy + lastModule.height).toFloat())
            } else {
                maxScroll = 0f
            }
            for (nlModule in visibleModules) {
                nlModule.x = x
                nlModule.y = y
                nlModule.w = w
                nlModule.h = h
                GL11.glEnable(GL11.GL_SCISSOR_TEST)
                RenderUtil.scissor((x + 90).toFloat(), (y + 40).toFloat(), (w - 90).toFloat(), (h - 40).toFloat())
                nlModule.draw(mx, my)
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
            }
        }
        if (isSelected && subCategory == Category.SubCategory.CONFIGS) {
            val scrolll = scroll
            NeverloseGui.getInstance().configs.scroll = scrolll.toInt()
            NeverloseGui.getInstance().configs.setBounds(x + 90, y + 40, w - 110)
            onScroll(40)
            maxScroll = max(0f, (NeverloseGui.getInstance().configs.contentHeight - (h - 40)).toFloat())
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            RenderUtil.scissor((x + 90).toFloat(), (y + 40).toFloat(), (w - 90).toFloat(), (h - 40).toFloat())
            NeverloseGui.getInstance().configs.draw(mx, my)
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    fun onScroll(ms: Int) {
        scroll = (rawScroll - scrollAnimation.output).toFloat()
        rawScroll += Mouse.getDWheel() / 4f
        rawScroll = max(minScroll, min(rawScroll, -maxScroll))
        scrollAnimation = SmoothStepAnimation(ms.toDouble(), (rawScroll - scroll).toDouble(), Direction.BACKWARDS)
    }

    val scroll: Float
        get() {
            scroll = (rawScroll - scrollAnimation.output).toFloat()
            return scroll
        }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        nlModules.forEach { it.keyTyped(typedChar, keyCode) }
    }

    fun released(mx: Int, my: Int, mb: Int) {
        nlModules.forEach { it.released(mx, my, mb) }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (isSelected && subCategory != Category.SubCategory.CONFIGS) {
            nlModules.forEach { it.click(mx, my, mb) }
        }
        if (isSelected && subCategory == Category.SubCategory.CONFIGS) {
            NeverloseGui.getInstance().configs.click(mx, my, mb)
        }
    }

    private val isSelected: Boolean
        get() = NeverloseGui.getInstance().selectedSub === this

    val layoutModules: List<NlModule>
        get() = if (visibleModules.isEmpty() && NeverloseGui.getInstance().isSearching) visibleModules else if (visibleModules.isEmpty()) nlModules else visibleModules

    private fun getVisibleModules(): List<NlModule> {
        if (!NeverloseGui.getInstance().isSearching) {
            return nlModules
        }
        val query = NeverloseGui.getInstance().searchTextContent.lowercase(Locale.getDefault())
        return nlModules.stream().filter { module: NlModule -> module.module.name.lowercase(Locale.getDefault()).contains(query) }.toList()
    }

    private val icon: String
        get() = subCategory.icon
}
