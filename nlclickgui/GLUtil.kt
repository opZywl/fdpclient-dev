package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBegin
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glEnd

object GLUtil {

    fun render(mode: Int, render: Runnable) {
        glBegin(mode)
        render.run()
        glEnd()
    }

    fun setup2DRendering(f: Runnable) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        f.run()
        glEnable(GL_TEXTURE_2D)
        GlStateManager.disableBlend()
    }

    fun rotate(x: Float, y: Float, rotate: Float, f: Runnable) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.rotate(rotate, 0f, 0f, -1f)
        GlStateManager.translate(-x, -y, 0f)
        f.run()
        GlStateManager.popMatrix()
    }
}
