package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.ShaderUtil
import net.ccbluex.liquidbounce.utils.extensions.calculateGaussianValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.nio.FloatBuffer

object GaussianBlur {
    @JvmField
    val blurShader = ShaderUtil("fdpclient/shaders/gaussian.frag")

    @JvmField
    var framebuffer = Framebuffer(1, 1, false)

    fun setupUniforms(dir1: Float, dir2: Float, radius: Float) {
        blurShader.setUniformi("textureIn", 0)
        blurShader.setUniformf("texelSize", 1.0f / RenderUtil.mc.displayWidth, 1.0f / RenderUtil.mc.displayHeight)
        blurShader.setUniformf("direction", dir1, dir2)
        blurShader.setUniformf("radius", radius)

        val weightBuffer: FloatBuffer = BufferUtils.createFloatBuffer(256)
        for (i in 0..radius.toInt()) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2f))
        }

        weightBuffer.rewind()
        OpenGlHelper.glUniform1(blurShader.getUniform("weights"), weightBuffer)
    }

    fun renderBlur(radius: Float) {
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        framebuffer = RenderUtil.createFrameBuffer(framebuffer)

        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(1f, 0f, radius)

        RenderUtil.bindTexture(RenderUtil.mc.framebuffer.framebufferTexture)

        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())
        framebuffer.unbindFramebuffer()
        blurShader.unload()

        RenderUtil.mc.framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(0f, 1f, radius)

        RenderUtil.bindTexture(framebuffer.framebufferTexture)
        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())
        blurShader.unload()

        RenderUtil.resetColor()
        GlStateManager.bindTexture(0)
    }
}
