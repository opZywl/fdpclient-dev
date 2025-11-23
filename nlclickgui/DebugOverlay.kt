package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

object NlDebugOverlay {
    data class FramebufferDebug(val rebuilt: Boolean, val hadDepthAttachment: Boolean, val hasDepthAfter: Boolean)
    data class BlurDebug(
        val passName: String,
        val depthEnabledBefore: Boolean,
        val depthMaskBefore: Boolean,
        val framebufferHasDepth: Boolean,
        val mainFramebufferHasDepth: Boolean
    )

    @Volatile
    private var framebufferDebug: FramebufferDebug? = null

    @Volatile
    private var bloomDebug: BlurDebug? = null

    @Volatile
    private var gaussianDebug: BlurDebug? = null

    var enabled: Boolean = true

    fun noteFramebuffer(needsRebuild: Boolean, hadDepthAttachment: Boolean, hasDepthAfter: Boolean) {
        if (!enabled) return

        framebufferDebug = FramebufferDebug(needsRebuild, hadDepthAttachment, hasDepthAfter)
    }

    fun noteBloom(depthEnabled: Boolean, depthMask: Boolean, framebufferHasDepth: Boolean, mainFramebufferHasDepth: Boolean) {
        if (!enabled) return

        bloomDebug = BlurDebug("Bloom", depthEnabled, depthMask, framebufferHasDepth, mainFramebufferHasDepth)
    }

    fun noteGaussian(depthEnabled: Boolean, depthMask: Boolean, framebufferHasDepth: Boolean, mainFramebufferHasDepth: Boolean) {
        if (!enabled) return

        gaussianDebug = BlurDebug("Gaussian", depthEnabled, depthMask, framebufferHasDepth, mainFramebufferHasDepth)
    }

    fun render(gui: NeverloseGui) {
        if (!enabled) return

        val lines = mutableListOf<String>()
        lines += "NLClickGUI Shadow Debug"

        framebufferDebug?.let {
            lines += "Framebuffer rebuilt: ${it.rebuilt}"
            lines += "Prev depth attachment: ${it.hadDepthAttachment}"
            lines += "Depth after rebuild: ${it.hasDepthAfter}"
        } ?: lines.add("Framebuffer: no data")

        listOfNotNull(bloomDebug, gaussianDebug).forEach { debug ->
            lines += "${debug.passName} depthTest: ${debug.depthEnabledBefore}"
            lines += "${debug.passName} depthMask: ${debug.depthMaskBefore}"
            lines += "${debug.passName} FB depth: ${debug.framebufferHasDepth}"
            lines += "${debug.passName} main depth: ${debug.mainFramebufferHasDepth}"
        }

        val font = Fonts.Nl_16
        val padding = 6f
        val lineHeight = font.height + 2
        val panelWidth = lines.maxOf { font.stringWidth(it) } + (padding * 2).toInt()
        val panelHeight = lineHeight * lines.size + padding.toInt()

        val drawX = (gui.x + gui.w + 12).toFloat()
        val drawY = gui.y.toFloat()

        GlStateManager.disableDepth()
        RenderUtil.drawRect(drawX, drawY, drawX + panelWidth, drawY + panelHeight, Color(0, 0, 0, 120))
        RenderUtil.drawRect(drawX, drawY, drawX + panelWidth, drawY + 1f, NeverloseGui.neverlosecolor)

        var offsetY = drawY + padding
        lines.forEach { line ->
            font.drawString(line, drawX + padding, offsetY, Color.WHITE.rgb)
            offsetY += lineHeight
        }
        GlStateManager.enableDepth()
    }
}
