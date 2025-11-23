package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl.GLClientState
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate.Tessellation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.MathHelper
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object RenderUtil {

    @JvmField
    val tessellator: Tessellation = Tessellation.createExpanding(4, 1.0f, 2.0f)

    @JvmField
    val mc: Minecraft = Minecraft.getMinecraft()

    private val csBuffer = mutableListOf<Int>()

    @JvmField
    var deltaTime = 0

    @JvmField
    var zLevel = 0f

    @JvmField
    var delta = 0f

    fun isHovered(x: Float, y: Float, x2: Float, y2: Float, mouseX: Int, mouseY: Int): Boolean = mouseX in x..x2 && mouseY in y..y2

    fun startRender() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_CULL_FACE)
    }

    fun stopRender() {
        glEnable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        color(Color.white)
    }

    fun color(color: Color) {
        glColor4d(color.red / 255.0, color.green / 255.0, color.blue / 255.0, color.alpha / 255.0)
    }

    private fun drawCircle(xPos: Double, yPos: Double, radius: Double) {
        val theta = 2 * Math.PI / 360.0
        val tangetialFactor = kotlin.math.tan(theta)
        val radialFactor = MathHelper.cos(theta.toFloat())
        var x = radius
        var y = 0.0
        repeat(360) {
            glVertex2d(x + xPos, y + yPos)
            val tx = -y
            val ty = x
            x += tx * tangetialFactor
            y += ty * tangetialFactor
            x *= radialFactor
            y *= radialFactor
        }
    }

    fun drawCircle(xPos: Double, yPos: Double, radius: Double, color: Color) {
        startRender()
        color(color)
        glBegin(GL_POLYGON)
        drawCircle(xPos, yPos, radius)
        glEnd()
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_LOOP)
        drawCircle(xPos, yPos, radius)
        glEnd()
        stopRender()
    }

    fun smoothAnimation(ani: Float, finalState: Float, speed: Float, scale: Float): Float =
        getAnimationState(ani, finalState, max(10.0f, abs(ani - finalState) * speed) * scale)

    fun drawFastRoundedRect(x0: Float, y0: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        glDisable(GL_CULL_FACE)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(770, 771)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        glColor4f(r, g, b, a)
        glBegin(GL_TRIANGLE_STRIP)
        glVertex2f(x0 + radius, y0)
        glVertex2f(x0 + radius, y1)
        glVertex2f(x1 - radius, y0)
        glVertex2f(x1 - radius, y1)
        glEnd()
        glBegin(GL_TRIANGLE_STRIP)
        glVertex2f(x0, y0 + radius)
        glVertex2f(x0 + radius, y0 + radius)
        glVertex2f(x0, y1 - radius)
        glVertex2f(x0 + radius, y1 - radius)
        glEnd()
        glBegin(GL_TRIANGLE_STRIP)
        glVertex2f(x1, y0 + radius)
        glVertex2f(x1 - radius, y0 + radius)
        glVertex2f(x1, y1 - radius)
        glVertex2f(x1 - radius, y1 - radius)
        glEnd()
        glBegin(GL_TRIANGLE_FAN)
        var f6 = x1 - radius
        var f7 = y0 + radius
        glVertex2f(f6, f7)
        for (j in 0..18) {
            val f8 = j * 5.0f
            glVertex2f(
                f6 + radius * MathHelper.cos(Math.toRadians(f8.toDouble()).toFloat()),
                f7 - radius * MathHelper.sin(Math.toRadians(f8.toDouble()).toFloat())
            )
        }
        glEnd()
        glBegin(GL_TRIANGLE_FAN)
        f6 = x0 + radius
        f7 = y0 + radius
        glVertex2f(f6, f7)
        for (j in 0..18) {
            val f9 = j * 5.0f
            glVertex2f(
                f6 - radius * MathHelper.cos(Math.toRadians(f9.toDouble()).toFloat()),
                f7 - radius * MathHelper.sin(Math.toRadians(f9.toDouble()).toFloat())
            )
        }
        glEnd()
        glBegin(GL_TRIANGLE_FAN)
        f6 = x0 + radius
        f7 = y1 - radius
        glVertex2f(f6, f7)
        for (j in 0..18) {
            val f10 = j * 5.0f
            glVertex2f(
                f6 - radius * MathHelper.cos(Math.toRadians(f10.toDouble()).toFloat()),
                f7 + radius * MathHelper.sin(Math.toRadians(f10.toDouble()).toFloat())
            )
        }
        glEnd()
        glBegin(GL_TRIANGLE_FAN)
        f6 = x1 - radius
        f7 = y1 - radius
        glVertex2f(f6, f7)
        for (j in 0..18) {
            val f11 = j * 5.0f
            glVertex2f(
                f6 + radius * MathHelper.cos(Math.toRadians(f11.toDouble()).toFloat()),
                f7 + radius * MathHelper.sin(Math.toRadians(f11.toDouble()).toFloat())
            )
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_CULL_FACE)
        glDisable(GL_BLEND)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun startGlScissor(x: Int, y: Int, width: Int, height: Int) {
        val scaleFactor = ScaledResolution(mc).scaleFactor
        glPushMatrix()
        glEnable(GL_SCISSOR_TEST)
        glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, (height + 14) * scaleFactor)
    }

    fun stopGlScissor() {
        glDisable(GL_SCISSOR_TEST)
        glPopMatrix()
    }

    fun convertRGB(rgb: Int): FloatArray {
        val a = (rgb shr 24 and 0xFF) / 255.0f
        val r = (rgb shr 16 and 0xFF) / 255.0f
        val g = (rgb shr 8 and 0xFF) / 255.0f
        val b = (rgb and 0xFF) / 255.0f
        return floatArrayOf(r, g, b, a)
    }

    fun toColorRGB(rgb: Int, alpha: Float): Color {
        val rgba = convertRGB(rgb)
        return Color(rgba[0], rgba[1], rgba[2], alpha / 255f)
    }

    fun color(color: Color, alpha: Float) {
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, alpha / 255f)
    }

    fun project2D(x: Double, y: Double, z: Double): DoubleArray? {
        val objectPosition = BufferUtils.createFloatBuffer(3)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        val viewport = BufferUtils.createIntBuffer(16)
        glGetFloat(GL_MODELVIEW_MATRIX, modelView)
        glGetFloat(GL_PROJECTION_MATRIX, projection)
        glGetInteger(GL_VIEWPORT, viewport)
        val sc = ScaledResolution(mc)
        return if (GLU.gluProject(x.toFloat(), y.toFloat(), z.toFloat(), modelView, projection, viewport, objectPosition))
            doubleArrayOf(objectPosition[0] / sc.scaleFactor, objectPosition[1] / sc.scaleFactor, objectPosition[2].toDouble()) else null
    }

    fun getAnimationStateSmooth(target: Double, currentIn: Double, speedIn: Double): Double {
        var current = currentIn
        var speed = speedIn
        val larger = target > current
        speed = speed.coerceIn(0.0, 1.0)
        if (target == current) return target
        var dif = max(target, current) - min(target, current)
        var factor = dif * speed
        if (factor < 0.1) factor = 0.1
        current = if (larger) {
            if (current + factor > target) target else current + factor
        } else {
            if (current - factor < target) target else current - factor
        }
        return current
    }

    fun doGlScissor(x: Int, y: Int, width: Int, height: Int) {
        val mc = Minecraft.getMinecraft()
        var scaleFactor = 1
        var k = mc.gameSettings.guiScale
        if (k == 0) k = 1000
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor)
    }

    fun getAnimationState(animationIn: Float, finalState: Float, speed: Float): Float {
        var animation = animationIn
        val add = delta * speed
        animation = if (animation < finalState) {
            if (animation + add < finalState) animation + add else finalState
        } else if (animation - add > finalState) {
            animation - add
        } else {
            finalState
        }
        return animation
    }

    fun enableGL2D() {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(770, 771)
        glDepthMask(true)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
    }

    fun disableGL2D() {
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)
    }

    private fun draw(renderer: WorldRenderer, x: Int, y: Int, width: Int, height: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        renderer.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        renderer.pos((x + width).toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        Tessellator.getInstance().draw()
    }

    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    fun pre3D() {
        glPushMatrix()
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glShadeModel(GL_SMOOTH)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glDepthMask(false)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
    }

    fun post3D() {
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
        glColor4f(1f, 1f, 1f, 1f)
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 255).toFloat() / 255.0f
        val f1 = (col1 shr 16 and 255).toFloat() / 255.0f
        val f2 = (col1 shr 8 and 255).toFloat() / 255.0f
        val f3 = (col1 and 255).toFloat() / 255.0f
        val f4 = (col2 shr 24 and 255).toFloat() / 255.0f
        val f5 = (col2 shr 16 and 255).toFloat() / 255.0f
        val f6 = (col2 shr 8 and 255).toFloat() / 255.0f
        val f7 = (col2 and 255).toFloat() / 255.0f
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(770, 771)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(GL_QUADS)
        glColor4f(f1, f2, f3, f)
        glVertex2d(left, bottom)
        glVertex2d(right, bottom)
        glColor4f(f5, f6, f7, f4)
        glVertex2d(right, top)
        glVertex2d(left, top)
        glEnd()
        glPopMatrix()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(7424)
        Gui.drawRect(0, 0, 0, 0, 0)
    }

    fun drawScaledCustomSizeModalRect(x: Float, y: Float, u: Float, v: Float, uWidth: Int, vHeight: Int, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        drawBoundTexture(x, y, u, v, uWidth.toFloat(), vHeight.toFloat(), width, height, tileWidth, tileHeight)
    }

    fun drawTracers(e: Entity?, color: Int, lw: Float) {
        if (e == null) return
        val x = e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosX
        val y = e.eyeHeight + e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosY
        val z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosZ
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        glPushMatrix()
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(770, 771)
        glEnable(GL_BLEND)
        glLineWidth(lw)
        glColor4f(r, g, b, a)
        glBegin(GL_LINES)
        glVertex3d(0.0, 0.0 + mc.thePlayer.eyeHeight.toDouble(), 0.0)
        glVertex3d(x, y, z)
        glEnd()
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun drawESP(e: Entity?, colorIn: Int, damage: Boolean, type: Int) {
        if (e == null) return
        var color = colorIn
        val x = e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosX
        val y = e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosY
        val z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.viewerPosZ
        if (e is EntityPlayer && damage && e.hurtTime != 0) color = Color.RED.rgb
        val a = (color shr 24 and 0xFF) / 255.0f
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        when (type) {
            1 -> {
                GlStateManager.pushMatrix()
                glBlendFunc(770, 771)
                glEnable(GL_BLEND)
                glDisable(GL_TEXTURE_2D)
                glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                glLineWidth(3.0f)
                glColor4f(r, g, b, a)
                RenderGlobal.drawSelectionBoundingBox(
                    AxisAlignedBB(
                        e.entityBoundingBox.minX - 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                        e.entityBoundingBox.minY - e.posY + (e.posY - mc.renderManager.viewerPosY),
                        e.entityBoundingBox.minZ - 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ),
                        e.entityBoundingBox.maxX + 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                        e.entityBoundingBox.maxY + 0.1 - e.posY + (e.posY - mc.renderManager.viewerPosY),
                        e.entityBoundingBox.maxZ + 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ)
                    )
                )
                drawAABB(
                    AxisAlignedBB(
                        e.entityBoundingBox.minX - 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                        e.entityBoundingBox.minY - e.posY + (e.posY - mc.renderManager.viewerPosY),
                        e.entityBoundingBox.minZ - 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ),
                        e.entityBoundingBox.maxX + 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                        e.entityBoundingBox.maxY + 0.1 - e.posY + (e.posY - mc.renderManager.viewerPosY),
                        e.entityBoundingBox.maxZ + 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ)
                    ), r, g, b
                )
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(true)
                glDisable(GL_BLEND)
                GlStateManager.popMatrix()
                glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            }
            2, 3 -> {
                val mode = type == 2
                glBlendFunc(770, 771)
                glEnable(GL_BLEND)
                glLineWidth(3.0f)
                glDisable(GL_TEXTURE_2D)
                glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                glColor4d(r.toDouble(), g.toDouble(), b.toDouble(), a.toDouble())
                if (mode) {
                    RenderGlobal.drawSelectionBoundingBox(
                        AxisAlignedBB(
                            e.entityBoundingBox.minX - 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                            e.entityBoundingBox.minY - e.posY + (e.posY - mc.renderManager.viewerPosY),
                            e.entityBoundingBox.minZ - 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ),
                            e.entityBoundingBox.maxX + 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                            e.entityBoundingBox.maxY + 0.1 - e.posY + (e.posY - mc.renderManager.viewerPosY),
                            e.entityBoundingBox.maxZ + 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ)
                        )
                    )
                } else {
                    drawAABB(
                        AxisAlignedBB(
                            e.entityBoundingBox.minX - 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                            e.entityBoundingBox.minY - e.posY + (e.posY - mc.renderManager.viewerPosY),
                            e.entityBoundingBox.minZ - 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ),
                            e.entityBoundingBox.maxX + 0.05 - e.posX + (e.posX - mc.renderManager.viewerPosX),
                            e.entityBoundingBox.maxY + 0.1 - e.posY + (e.posY - mc.renderManager.viewerPosY),
                            e.entityBoundingBox.maxZ + 0.05 - e.posZ + (e.posZ - mc.renderManager.viewerPosZ)
                        ), r, g, b
                    )
                }
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(true)
                glDisable(GL_BLEND)
            }
        }
    }

    fun drawAABB(axisAlignedBB: AxisAlignedBB, r: Float, g: Float, b: Float) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(r, g, b, 0.25f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(r, g, b, 0.25f).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(r, g, b, 1.0f).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawTexturedModalRect(x: Double, y: Double, textureX: Double, textureY: Double, width: Double, height: Double) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x, y + height, zLevel.toDouble()).tex((textureX + 0) * f, (textureY + height) * f1).endVertex()
        worldrenderer.pos(x + width, y + height, zLevel.toDouble()).tex((textureX + width) * f, (textureY + height) * f1).endVertex()
        worldrenderer.pos(x + width, y + 0, zLevel.toDouble()).tex((textureX + width) * f, (textureY + 0) * f1).endVertex()
        worldrenderer.pos(x + 0, y + 0, zLevel.toDouble()).tex((textureX + 0) * f, (textureY + 0) * f1).endVertex()
        tessellator.draw()
    }

    private fun drawBoundTexture(x: Float, y: Float, u: Float, v: Float, uWidth: Float, vHeight: Float, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(u * f, (v + vHeight) * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0).tex((u + uWidth) * f, v * f1).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex(u * f, v * f1).endVertex()
        tessellator.draw()
    }

    fun reAlpha(color: Int, alpha: Float): Int {
        return try {
            val c = Color(color)
            val r = (1f / 255) * c.red
            val g = (1f / 255) * c.green
            val b = (1f / 255) * c.blue
            Color(r, g, b, alpha).rgb
        } catch (e: Throwable) {
            e.printStackTrace()
            color
        }
    }

    fun drawArrow(x: Double, y: Double, lineWidth: Int, color: Int, length: Double) {
        start2D()
        glPushMatrix()
        glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        glBegin(GL_LINE_STRIP)
        glVertex2d(x, y)
        glVertex2d(x + 3, y + length)
        glVertex2d(x + 6, y)
        glEnd()
        glPopMatrix()
        stop2D()
    }

    fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL_GREATER, limit * .01f)
    }

    fun fakeCircleGlow(posX: Float, posY: Float, radius: Float, color: Color, maxAlpha: Float) {
        setAlphaLimit(0f)
        glShadeModel(GL_SMOOTH)
        GLUtil.setup2DRendering(Runnable {
            GLUtil.render(GL_TRIANGLE_FAN, Runnable {
                color(color.rgb, maxAlpha)
                glVertex2d(posX.toDouble(), posY.toDouble())
                color(color.rgb, 0f)
                for (i in 0..100) {
                    val angle = i * .06283 + 3.1415
                    val x2 = kotlin.math.sin(angle) * radius
                    val y2 = kotlin.math.cos(angle) * radius
                    glVertex2d(posX + x2, posY + y2)
                }
            })
        })
        glShadeModel(GL_FLAT)
        setAlphaLimit(1f)
    }

    fun brighter(color: Color, factor: Float): Color {
        var r = color.red
        var g = color.green
        var b = color.blue
        val alpha = color.alpha
        val i = (1.0 / (1.0 - factor)).toInt()
        if (r == 0 && g == 0 && b == 0) return Color(i, i, i, alpha)
        if (r in 1 until i) r = i
        if (g in 1 until i) g = i
        if (b in 1 until i) b = i
        return Color(min((r / factor).toInt(), 255), min((g / factor).toInt(), 255), min((b / factor).toInt(), 255), alpha)
    }

    fun applyOpacity(color: Color, opacityIn: Float): Color {
        val opacity = opacityIn.coerceIn(0f, 1f)
        return Color(color.red, color.green, color.blue, (color.alpha * opacity).toInt())
    }

    fun interpolateColorC(color1: Color, color2: Color, amountIn: Float): Color {
        val amount = amountIn.coerceIn(0f, 1f)
        fun interp(a: Int, b: Int): Int = (a + (b - a) * amount).toInt()
        return Color(interp(color1.red, color2.red), interp(color1.green, color2.green), interp(color1.blue, color2.blue), interp(color1.alpha, color2.alpha))
    }

    fun animate(endPoint: Double, current: Double, speedIn: Double): Double {
        var speed = speedIn
        val shouldContinueAnimation = endPoint > current
        speed = speed.coerceIn(0.0, 1.0)
        val dif = max(endPoint, current) - min(endPoint, current)
        val factor = dif * speed
        return current + if (shouldContinueAnimation) factor else -factor
    }

    fun drawRect2(x: Double, y: Double, width: Double, height: Double, color: Int) {
        resetColor()
        GLUtil.setup2DRendering(Runnable {
            RenderUtil.render(GL_QUADS, Runnable {
                color(color)
                glVertex2d(x, y)
                glVertex2d(x, y + height)
                glVertex2d(x + width, y + height)
                glVertex2d(x + width, y)
            })
        })
    }

    fun drawArc(x: Float, y: Float, start: Float, end: Float, radius: Float, color: Int) = arcEllipse(x, y, start, end, radius, radius, color)

    fun arcEllipse(x: Float, y: Float, startIn: Float, endIn: Float, w: Float, h: Float, color: Int) {
        var start = startIn
        var end = endIn
        if (start > end) {
            val temp = end
            end = start
            start = temp
        }
        val var11 = (color shr 24 and 0xFF) / 255.0f
        val var12 = (color shr 16 and 0xFF) / 255.0f
        val var13 = (color shr 8 and 0xFF) / 255.0f
        val var14 = (color and 0xFF) / 255.0f
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(var12, var13, var14, var11)
        if (var11 > 0.5f) {
            glEnable(GL_LINE_SMOOTH)
            glLineWidth(2.0f)
            glBegin(GL_LINE_STRIP)
            var i = end.toInt() - 1
            while (i >= start.toInt()) {
                val ldx = kotlin.math.cos((i - 1).toDouble() * Math.PI / 180.0) * (w * 1.001)
                val ldy = kotlin.math.sin((i - 1).toDouble() * Math.PI / 180.0) * (h * 1.001)
                val dx = kotlin.math.cos(i.toDouble() * Math.PI / 180.0) * (w * 1.001)
                val dy = kotlin.math.sin(i.toDouble() * Math.PI / 180.0) * (h * 1.001)
                glVertex2d((x + dx), (y + dy))
                glVertex2d((x + ldx), (y + ldy))
                i--
            }
            glEnd()
            glDisable(GL_LINE_SMOOTH)
        }
        glBegin(GL_TRIANGLE_FAN)
        var i = end.toInt() - 1
        while (i >= start.toInt()) {
            val dx = kotlin.math.cos(i.toDouble() * Math.PI / 180.0) * w
            val dy = kotlin.math.sin(i.toDouble() * Math.PI / 180.0) * h
            glVertex2d((x + dx), (y + dy))
            i--
        }
        glVertex2d(x.toDouble(), y.toDouble())
        glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawCheckeredBackground(x: Float, yIn: Float, x2: Float, y2In: Float) {
        var y = yIn
        val y2 = y2In
        drawRect(x, y, x2, y2, getColor(16777215))
        var offset = false
        while (y < y2) {
            var x1 = x + if (offset) 1 else 0
            offset = !offset
            while (x1 < x2) {
                if (x1 <= x2 - 1.0f) drawRect(x1, y, x1 + 1.0f, y + 1.0f, getColor(8421504))
                x1 += 2.0f
            }
            y += 1f
        }
    }

    fun drawStack(font: net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer, renderOverlay: Boolean, stack: ItemStack, x: Float, y: Float) {
        glPushMatrix()
        if (mc.theWorld != null) RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        GlStateManager.enableBlend()
        mc.renderItem.zLevel = -150.0f
        mc.renderItem.renderItemAndEffectIntoGUI(stack, x.toInt(), y.toInt())
        if (renderOverlay) renderItemOverlayIntoGUI(font, stack, x.toInt(), y.toInt(), stack.stackSize.toString())
        mc.renderItem.zLevel = 0.0f
        GlStateManager.enableBlend()
        val z = 0.5f
        GlStateManager.scale(z, z, z)
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.scale(1f, 2.0f, 2.0f)
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        glPopMatrix()
    }

    fun renderItemOverlayIntoGUI(fr: net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer, stack: ItemStack?, xPosition: Int, yPosition: Int, text: String?) {
        if (stack == null) return
        if (stack.stackSize != 1 || text != null) {
            var s = text ?: stack.stackSize.toString()
            if (text == null && stack.stackSize < 1) s = EnumChatFormatting.RED.toString() + stack.stackSize
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            fr.drawString(s, (xPosition + 19 - 2 - fr.stringWidth(s)).toFloat(), (yPosition + 6 + 3).toFloat(), 16777215)
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
        if (stack.isItemDamaged) {
            val durability = stack.itemDamage.toDouble() / stack.maxDamage.toDouble()
            val j = kotlin.math.round(13.0 - durability * 13.0).toInt()
            val i = kotlin.math.round(255.0 - durability * 255.0).toInt()
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableTexture2D()
            GlStateManager.disableAlpha()
            GlStateManager.disableBlend()
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer
            draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255)
            draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255)
            draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255)
            GlStateManager.enableAlpha()
            GlStateManager.enableTexture2D()
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
    }

    fun drawCheck(x: Double, y: Double, lineWidth: Int, color: Int) {
        start2D()
        glPushMatrix()
        glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        glBegin(GL_LINE_STRIP)
        glVertex2d(x, y)
        glVertex2d(x + 2, y + 3)
        glVertex2d(x + 6, y - 2)
        glEnd()
        glPopMatrix()
        stop2D()
    }

    fun setGLColor(color: Int) = setGLColor(Color(color))

    fun setColor(color: Color) {
        val alpha = (color.rgb shr 24 and 0xFF) / 255.0f
        val red = (color.rgb shr 16 and 0xFF) / 255.0f
        val green = (color.rgb shr 8 and 0xFF) / 255.0f
        val blue = (color.rgb and 0xFF) / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    fun setColor(colorHex: Int) {
        val alpha = (colorHex shr 24 and 255).toFloat() / 255.0f
        val red = (colorHex shr 16 and 255).toFloat() / 255.0f
        val green = (colorHex shr 8 and 255).toFloat() / 255.0f
        val blue = (colorHex and 255).toFloat() / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    fun setGLColor(color: Color) {
        val r = color.red / 255f
        val g = color.green / 255f
        val b = color.blue / 255f
        val a = color.alpha / 255f
        GlStateManager.color(r, g, b, a)
    }

    fun renderBuffer(range: IntRange, mode: Int) {
        start2D()
        glLineWidth(2f)
        glBegin(mode)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_POINT_SMOOTH)
        range.forEach { glArrayElement(it) }
        glDisable(GL_POINT_SMOOTH)
        glDisable(GL_LINE_SMOOTH)
        glEnd()
        stop2D()
    }

    fun pushScissor() {
        csBuffer.add(glGetInteger(GL_SCISSOR_BOX))
    }

    fun popScissor() {
        val scissor = csBuffer.removeAt(csBuffer.size - 1)
        glScissor(scissor and 0xFFFF, scissor shr 16 and 0xFFFF, scissor shr 32 and 0xFFFF, scissor shr 48 and 0xFFFF)
    }

    fun scissor(x: Float, y: Float, x1: Float, y1: Float) {
        val scaleFactor = ScaledResolution(mc).scaleFactor
        val scissorY = mc.displayHeight - y1.toInt() * scaleFactor
        glScissor((x * scaleFactor).toInt(), scissorY, ((x1 - x) * scaleFactor).toInt(), ((y1 - y) * scaleFactor).toInt())
    }

    fun drawImage(x: Float, y: Float, width: Float, height: Float, image: net.minecraft.util.ResourceLocation) {
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glDepthMask(false)
        glBlendFunc(770, 771)
        glEnable(GL_TEXTURE_2D)
        GlStateManager.enableTexture2D()
        mc.textureManager.bindTexture(image)
        glColor4f(1f, 1f, 1f, 1f)
        glBegin(GL_QUADS)
        glTexCoord2f(0.0f, 0.0f)
        glVertex2f(x, y)
        glTexCoord2f(0.0f, 1.0f)
        glVertex2f(x, y + height)
        glTexCoord2f(1.0f, 1.0f)
        glVertex2f(x + width, y + height)
        glTexCoord2f(1.0f, 0.0f)
        glVertex2f(x + width, y)
        glEnd()
        GlStateManager.disableBlend()
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
    }

    fun setColor(color: Int, alpha: Float) {
        val f = (color shr 24 and 0xFF) / 255.0f
        val f1 = (color shr 16 and 0xFF) / 255.0f
        val f2 = (color shr 8 and 0xFF) / 255.0f
        val f3 = (color and 0xFF) / 255.0f
        glColor4f(f1, f2, f3, alpha / 255f * f)
    }

    fun color(color: Int) {
        val alpha = (color shr 24 and 255).toFloat() / 255.0f
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) = RoundedUtil.drawRound(x, y, width, height, radius, color)

    fun drawRoundedRect(x: Double, y: Double, x2: Double, y2: Double, round: Double, color: Int) = RoundedUtil.drawRound(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), round.toFloat(), color)

    fun drawRect(x: Float, y: Float, x1: Float, y1: Float, color: Int) {
        val alpha = (color shr 24 and 255).toFloat() / 255.0f
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        glColor4f(red, green, blue, alpha)
        glBegin(GL_QUADS)
        glVertex2f(x1, y)
        glVertex2f(x, y)
        glVertex2f(x, y1)
        glVertex2f(x1, y1)
        glEnd()
    }

    fun drawRect(x: Double, y: Double, x1: Double, y1: Double, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(770, 771)
        glEnable(GL_LINE_SMOOTH)
        color(color)
        glBegin(GL_QUADS)
        glVertex2d(x1, y)
        glVertex2d(x, y)
        glVertex2d(x, y1)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, thickness: Float) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(770, 771)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thickness)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawLines(x: Float, y: Float, x1: Float, y1: Float, thickness: Float) {
        glLineWidth(thickness)
        glBegin(GL_LINES)
        glVertex2f(x, y)
        glVertex2f(x1, y1)
        glEnd()
    }

    fun drawGradient(x: Float, y: Float, x1: Float, y1: Float, topColor: Int, bottomColor: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glShadeModel(7425)
        glBegin(GL_QUADS)
        color(topColor)
        glVertex2f(x, y)
        glVertex2f(x, y1)
        color(bottomColor)
        glVertex2f(x1, y1)
        glVertex2f(x1, y)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glShadeModel(7424)
    }

    fun start2D() {
        glEnable(GL_BLEND)
        glDisable(GL_CULL_FACE)
        glDisable(GL_TEXTURE_2D)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glBlendFunc(770, 771)
        glLineWidth(1f)
    }

    fun stop2D() {
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_CULL_FACE)
        glDisable(GL_BLEND)
    }

    fun resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, mouseX: Float, mouseY: Float, ent: net.minecraft.entity.EntityLivingBase) {
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX.toFloat(), posY.toFloat(), 50.0f)
        GlStateManager.scale((-scale).toFloat(), scale.toFloat(), scale.toFloat())
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
        val f = ent.renderYawOffset
        val f1 = ent.rotationYaw
        val f2 = ent.rotationPitch
        val f3 = ent.prevRotationYawHead
        val f4 = ent.rotationYawHead
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-Math.atan((mouseY / 40.0f).toDouble()).toFloat() * 20.0f, 1.0f, 0.0f, 0.0f)
        ent.renderYawOffset = (Math.atan((mouseX / 40.0f).toDouble()) * 20.0f).toFloat()
        ent.rotationYaw = (Math.atan((mouseX / 40.0f).toDouble()) * 40.0f).toFloat()
        ent.rotationPitch = (-Math.atan((mouseY / 40.0f).toDouble()) * 20.0f).toFloat()
        ent.rotationYawHead = ent.rotationYaw
        ent.prevRotationYawHead = ent.rotationYaw
        GlStateManager.translate(0.0f, 0.0f, 0.0f)
        val renderManager = Minecraft.getMinecraft().renderManager
        renderManager.playerViewY = 180.0f
        renderManager.isRenderShadow = false
        renderManager.doRenderEntity(ent, 0.0, 0.0, 0.0, 0.0f, 1.0f, true)
        renderManager.isRenderShadow = true
        ent.renderYawOffset = f
        ent.rotationYaw = f1
        ent.rotationPitch = f2
        ent.prevRotationYawHead = f3
        ent.rotationYawHead = f4
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun GLPre() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glDisable(GL_LIGHTING)
        glDepthMask(true)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
    }

    fun GLPost() {
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glEnable(GL_LIGHTING)
        glDepthMask(true)
    }

    fun setAlpha(alpha: Int) {
        val prevColor = GL11.glGetFloat(GL11.GL_CURRENT_COLOR)
        GL11.glColor4f(prevColor, prevColor, prevColor, alpha / 255f)
    }

    fun setAlpha(color: Color, alpha: Int) {
        GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, alpha / 255f)
    }

    fun setAlpha(color: Int, alpha: Int) {
        val r = (color shr 16 and 255).toFloat() / 255f
        val g = (color shr 8 and 255).toFloat() / 255f
        val b = (color and 255).toFloat() / 255f
        GL11.glColor4f(r, g, b, alpha / 255f)
    }

    fun getColor(brightness: Int, alpha: Int = 255): Int = Color(brightness, brightness, brightness, alpha).rgb

    fun rect(x: Double, y: Double, width: Double, height: Double, color: Int, fill: Boolean = true) {
        val r = color shr 16 and 255
        val g = color shr 8 and 255
        val b = color and 255
        val a = color shr 24 and 255
        GL11.glColor4f(r / 255f, g / 255f, b / 255f, a / 255f)
        GL11.glBegin(if (fill) GL11.GL_QUADS else GL11.GL_LINES)
        GL11.glVertex2d(x, y)
        GL11.glVertex2d(x + width, y)
        GL11.glVertex2d(x + width, y + height)
        GL11.glVertex2d(x, y + height)
        GL11.glEnd()
    }

    fun ease(factor: Double): Double = 1 - kotlin.math.pow(1 - factor, 3.0)

    fun drawPlayer(x: Float, y: Float, width: Int, height: Int, player: net.minecraft.entity.player.EntityPlayer, resolution: ScaledResolution) {
        player.textureData?.let { texture ->
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDepthMask(false)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            player.textureData?.bindTexture()
            Tessellation.draw2D(System.currentTimeMillis(), x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), 0.0, 0.0)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        }
    }

    fun depth(enable: Boolean) {
        if (enable) {
            glEnable(GL_DEPTH_TEST)
            GlStateManager.depthMask(true)
        } else {
            glDisable(GL_DEPTH_TEST)
            GlStateManager.depthMask(false)
        }
    }

    fun blend(enable: Boolean) {
        if (enable) {
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        } else {
            GlStateManager.disableBlend()
        }
    }

    fun glColor(color: Color) {
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
    }

    fun resetCaps() {
        Tessellation.releaseGL() ?: return
        GLClientState.values().forEach { it.cap.glDisable() }
    }

    fun enableGlCap(cap: Int) {
        GL11.glEnable(cap)
    }

    fun disableGlCap(cap: Int) {
        GL11.glDisable(cap)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        if (state) enableGlCap(cap) else disableGlCap(cap)
    }

    fun resetCaps(vararg caps: Int) {
        caps.forEach { glEnable(it) }
    }

    fun replaceAlpha(color: Int, alpha: Int): Int {
        val col = Color(color, true)
        return Color(col.red, col.green, col.blue, alpha).rgb
    }

    fun releaseGL() {
        resetCaps()
    }

    fun drawLine(width: Double, startPos: net.minecraft.util.Vec3, endPos: net.minecraft.util.Vec3) {
        glPushMatrix()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glLineWidth(width.toFloat())
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex3d(startPos.xCoord, startPos.yCoord, startPos.zCoord)
        GL11.glVertex3d(endPos.xCoord, endPos.yCoord, endPos.zCoord)
        GL11.glEnd()
        glPopMatrix()
    }
}
