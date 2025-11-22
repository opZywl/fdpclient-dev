package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;


import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl.GLClientState;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.tessellate.Tessellation;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.MathUtil.interpolateInt;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static org.lwjgl.opengl.GL11.*;

public class RenderUtil
{
    public static final Tessellation tessellator;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static final List<Integer> csBuffer;
    private static final Consumer<Integer> ENABLE_CLIENT_STATE;
    private static final Consumer<Integer> DISABLE_CLIENT_STATE;
    public static int deltaTime;

    protected static float zLevel;

    public static float delta;

    public static boolean isHovered(final float x, final float y, final float x2, final float y2, final int mouseX, final int mouseY) {
        return mouseX >= x && mouseX <= x2 && mouseY >= y && mouseY <= y2;
    }

    /**
     * Sets up basic rendering parameters
     */
    public static void startRender() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    /**
     * Resets the rendering parameters
     */
    public static void stopRender() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        color(Color.white);
    }

    /**
     * Sets the current color using rgb values
     *
     * @param color
     */
    public static void color(final Color color) {
        GL11.glColor4d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0, color.getAlpha() / 255.0);
    }

    private static void drawCircle(final double xPos, final double yPos, final double radius) {
        final double theta = (2 * Math.PI / 360.0);
        final double tangetial_factor = Math.tan(theta);//calculate the tangential factor
        final double radial_factor = MathHelper.cos((float) theta);//calculate the radial factor
        double x = radius;//we start at angle = 0
        double y = 0;
        for (int i = 0; i < 360; i++) {
            glVertex2d(x + xPos, y + yPos);

            //calculate the tangential vector
            //remember, the radial vector is (x, y)
            //to get the tangential vector we flip those coordinates and negate one of them
            final double tx = -y;
            final double ty = x;

            //add the tangential vector
            x += tx * tangetial_factor;
            y += ty * tangetial_factor;

            //correct using the radial factor
            x *= radial_factor;
            y *= radial_factor;
        }
    }

    public static void drawCircle(final double xPos, final double yPos, final double radius, final Color color) {
        startRender();
        color(color);
        glBegin(GL_POLYGON);
        {
            drawCircle(xPos, yPos, radius);
        }
        glEnd();

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2);
        glBegin(GL_LINE_LOOP);
        {
            drawCircle(xPos, yPos, radius);
        }
        glEnd();
        stopRender();
    }

    public static float smoothAnimation(float ani, float finalState, float speed, float scale) {
        return getAnimationState(ani, finalState, Math.max(10.0F, Math.abs(ani - finalState) * speed) * scale);
    }


    public static void drawFastRoundedRect(final float x0, final float y0, final float x1, final float y1, final float radius, final int color) {
        final float f2 = (color >> 24 & 0xFF) / 255.0f;
        final float f3 = (color >> 16 & 0xFF) / 255.0f;
        final float f4 = (color >> 8 & 0xFF) / 255.0f;
        final float f5 = (color & 0xFF) / 255.0f;
        glDisable(2884);
        glDisable(3553);
        glEnable(3042);

        glBlendFunc(770, 771);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        glColor4f(f3, f4, f5, f2);
        glBegin(5);
        glVertex2f(x0 + radius, y0);
        glVertex2f(x0 + radius, y1);
        glVertex2f(x1 - radius, y0);
        glVertex2f(x1 - radius, y1);
        glEnd();
        glBegin(5);
        glVertex2f(x0, y0 + radius);
        glVertex2f(x0 + radius, y0 + radius);
        glVertex2f(x0, y1 - radius);
        glVertex2f(x0 + radius, y1 - radius);
        glEnd();
        glBegin(5);
        glVertex2f(x1, y0 + radius);
        glVertex2f(x1 - radius, y0 + radius);
        glVertex2f(x1, y1 - radius);
        glVertex2f(x1 - radius, y1 - radius);
        glEnd();
        glBegin(6);
        float f6 = x1 - radius;
        float f7 = y0 + radius;
        glVertex2f(f6, f7);
        int j;
        for (j = 0; j <= 18; ++j) {
            final float f8 = j * 5.0f;
            glVertex2f((float) (f6 + radius * MathHelper.cos((float) Math.toRadians(f8))), (float) (f7 - radius * MathHelper.sin((float) Math.toRadians(f8))));
        }
        glEnd();
        glBegin(6);
        f6 = x0 + radius;
        f7 = y0 + radius;
        glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            final float f9 = j * 5.0f;
            glVertex2f((float) (f6 - radius * MathHelper.cos((float) Math.toRadians(f9))), (float) (f7 - radius * MathHelper.sin((float) Math.toRadians(f9))));
        }
        glEnd();
        glBegin(6);
        f6 = x0 + radius;
        f7 = y1 - radius;
        glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            final float f10 = j * 5.0f;
            glVertex2f((float) (f6 - radius * MathHelper.cos((float) Math.toRadians(f10))), (float) (f7 + radius * MathHelper.sin((float) Math.toRadians(f10))));
        }
        glEnd();
        glBegin(6);
        f6 = x1 - radius;
        f7 = y1 - radius;
        glVertex2f(f6, f7);
        for (j = 0; j <= 18; ++j) {
            final float f11 = j * 5.0f;
            glVertex2f((float) (f6 + radius * MathHelper.cos((float) Math.toRadians(f11))), (float) (f7 + radius * MathHelper.sin((float) Math.toRadians(f11))));
        }
        glEnd();
        glEnable(3553);
        glEnable(2884);
        glDisable(3042);
        enableTexture2D();
        disableBlend();
    }

    public static void startGlScissor(final int x, final int y, final int width, final int height) {
        final int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        glPushMatrix();
        glEnable(3089);
        glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, (height + 14) * scaleFactor);
    }

    public static void stopGlScissor() {
        glDisable(3089);
        glPopMatrix();
    }

    public static float[] convertRGB(int rgb) {
        float a = (rgb >> 24 & 0xFF) / 255.0f;
        float r = (rgb >> 16 & 0xFF) / 255.0f;
        float g = (rgb >> 8 & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return new float[]{r, g, b, a};
    }
    public static Color toColorRGB(int rgb, float alpha) {
        float[] rgba = convertRGB(rgb);
        return new Color(rgba[0], rgba[1], rgba[2], alpha / 255f);
    }

    public static void color(Color color, float alpha) {
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f);
    }
    public static double[] project2D(final double x, final double y, final double z) {
        FloatBuffer objectPosition = ActiveRenderInfo.objectCoords();
        ScaledResolution sc = new ScaledResolution(mc);
        if (GLU.gluProject((float)x, (float)y, (float)z, ActiveRenderInfo.modelview(), ActiveRenderInfo.projection(), ActiveRenderInfo.viewport(), objectPosition))
            return new double[]{ objectPosition.get(0) / sc.getScaleFactor(), objectPosition.get(1) / sc.getScaleFactor(),
                    objectPosition.get(2) };
        return null;
    }
    public static double getAnimationStateSmooth(double target, double current, double speed) {
        boolean larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }

        if (target == current) {
            return target;
        } else {
            double dif = Math.max(target, current) - Math.min(target, current);
            double factor = dif * speed;
            if (factor < 0.1) {
                factor = 0.1;
            }

            if (larger) {
                if (current + factor > target) {
                    current = target;
                } else {
                    current += factor;
                }
            } else if (current - factor < target) {
                current = target;
            } else {
                current -= factor;
            }

            return current;
        }
    }

    public static void doGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }

        while(scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
    }

    public static float getAnimationState(float animation, float finalState, float speed) {
        float add = delta * speed;
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }

        return animation;
    }


    public static void enableGL2D() {
        glDisable(2929);
        glEnable(3042);
        glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        glEnable(3553);
        glDisable(3042);
        glEnable(2929);
        glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }
    private static void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return framebuffer;
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LINE_SMOOTH);
        glDisable(GL11.GL_DEPTH_TEST);
        glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    public static void post3D() {
        GL11.glDepthMask(true);
        glEnable(GL11.GL_DEPTH_TEST);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable((int) 3042);
        GL11.glDisable((int) 3553);
        GL11.glBlendFunc((int) 770, (int) 771);
        GL11.glEnable((int) 2848);
        GL11.glShadeModel((int) 7425);
        GL11.glPushMatrix();
        GL11.glBegin((int) 7);
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
        GL11.glVertex2d((double) left, (double) bottom);
        GL11.glVertex2d((double) right, (double) bottom);
        GL11.glColor4f((float) f5, (float) f6, (float) f7, (float) f4);
        GL11.glVertex2d((double) right, (double) top);
        GL11.glVertex2d((double) left, (double) top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int) 3553);
        GL11.glDisable((int) 3042);
        GL11.glDisable((int) 2848);
        GL11.glShadeModel((int) 7424);
        Gui.drawRect(0, 0, 0, 0, 0);
    }

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        drawBoundTexture(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
    }

    public static void drawTracers(final Entity e, final int color, final float lw) {
        if (e == null) {
            return;
        }
        final double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
        final double y = e.getEyeHeight() + e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
        final double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;
        final float a = (color >> 24 & 0xFF) / 255.0f;
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(lw);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(2);
        GL11.glVertex3d(0.0, 0.0 + mc.thePlayer.getEyeHeight(), 0.0);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void drawESP(final Entity e, int color, final boolean damage, final int type) {
        if (e == null) {
            return;
        }
        final double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX;
        final double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY;
        final double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ;
        if (e instanceof EntityPlayer && damage && ((EntityPlayer) e).hurtTime != 0) {
            color = Color.RED.getRGB();
        }
        final float a = (color >> 24 & 0xFF) / 255.0f;
        final float r = (color >> 16 & 0xFF) / 255.0f;
        final float g = (color >> 8 & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        if (type == 1) {
            GlStateManager.pushMatrix();
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glLineWidth(3.0f);
            GL11.glColor4f(r, g, b, a);
            RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ), e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)));
            drawAABB(new AxisAlignedBB(e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ), e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)), r, g, b);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GlStateManager.popMatrix();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (type == 2 || type == 3) {
            final boolean mode = type == 2;
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glLineWidth(3.0f);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glColor4d(r, g, b, a);
            if (mode) {
                RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ), e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)));
            } else {
                drawAABB(new AxisAlignedBB(e.getEntityBoundingBox().minX - 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().minY - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().minZ - 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ), e.getEntityBoundingBox().maxX + 0.05 - e.posX + (e.posX - mc.getRenderManager().viewerPosX), e.getEntityBoundingBox().maxY + 0.1 - e.posY + (e.posY - mc.getRenderManager().viewerPosY), e.getEntityBoundingBox().maxZ + 0.05 - e.posZ + (e.posZ - mc.getRenderManager().viewerPosZ)), r, g, b);
            }
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
        } else if (type == 4) {
            GL11.glPushMatrix();
            GL11.glTranslated(x, y - 0.2, z);
            GL11.glScalef(0.03f, 0.03f, 0.03f);
            GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
            GlStateManager.disableDepth();
            Gui.drawRect(-20, -1, -26, 75, Color.black.getRGB());
            Gui.drawRect(-21, 0, -25, 74, color);
            Gui.drawRect(20, -1, 26, 75, Color.black.getRGB());
            Gui.drawRect(21, 0, 25, 74, color);
            Gui.drawRect(-20, -1, 21, 5, Color.black.getRGB());
            Gui.drawRect(-21, 0, 24, 4, color);
            Gui.drawRect(-20, 70, 21, 75, Color.black.getRGB());
            Gui.drawRect(-21, 71, 25, 74, color);
            GlStateManager.enableDepth();
            GL11.glPopMatrix();
        }
    }
    public static void drawAABB(final AxisAlignedBB aabb, final float r, final float g, final float b) {
        final float a = 0.25f;
        final Tessellator ts = Tessellator.getInstance();
        final WorldRenderer vb = ts.getWorldRenderer();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
    }
    public static void drawCornerBox(double x, double y, double x2, double y2, double lw, Color color) {
        double width = Math.abs(x2 - x);
        double height = Math.abs(y2 - y);
        double halfWidth = width / 4;
        double halfHeight = height / 4;
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth((float) lw);
        setColor(color);

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + halfWidth, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y + halfHeight);
        GL11.glEnd();


        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y + height - halfHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(x + halfWidth, y + height);
        GL11.glEnd();

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + width - halfWidth, y + height);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width, y + height - halfHeight);
        GL11.glEnd();

        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x + width, y + halfHeight);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width - halfWidth, y);
        GL11.glEnd();

        GL11.glPopMatrix();
        stop2D();
    }

    //XRay From novoline.cc


    public static void drawSolidBlockESP(BlockPos pos, int color) {
        double xPos = pos.getX() - mc.getRenderManager().renderPosX, yPos = pos.getY() - mc.getRenderManager().renderPosY, zPos = pos.getZ() - mc.getRenderManager().renderPosZ;
        double height = mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMaxY() - mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMinY();
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(f, f2, f3, f4);
        drawOutlinedBoundingBox(new AxisAlignedBB(xPos, yPos, zPos, xPos + 1.0, yPos + height, zPos + 1.0));
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glDisable(3042);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(2929);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    public static void drawLine(BlockPos blockPos, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        double renderPosXDelta = blockPos.getX() - mc.getRenderManager().renderPosX + 0.5D;
        double renderPosYDelta = blockPos.getY() - mc.getRenderManager().renderPosY + 0.5D;
        double renderPosZDelta = blockPos.getZ() - mc.getRenderManager().renderPosZ + 0.5D;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0F);
        float f = (float) (color >> 16 & 0xFF) / 255.0f;
        float f2 = (float) (color >> 8 & 0xFF) / 255.0f;
        float f3 = (float) (color & 0xFF) / 255.0f;
        float f4 = (float) (color >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(f, f2, f3, f4);
        GL11.glLoadIdentity();
        boolean previousState = mc.gameSettings.viewBobbing;
        mc.gameSettings.viewBobbing = false;
        (mc.entityRenderer).orientCamera(mc.timer.renderPartialTicks);
        GL11.glBegin(3);
        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glVertex3d(renderPosXDelta, renderPosYDelta, renderPosZDelta);
        GL11.glEnd();
        mc.gameSettings.viewBobbing = previousState;
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
    public static void drawOutlinedBoundingBox(AxisAlignedBB axisAlignedBB) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }
    public static void drawTexturedModalRect(double x, double y, double textureX, double textureY, double width,
                                             double height) {
        float f = 0.00390625F ;
        float f1 = 0.00390625F ;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x + 0, y + height, zLevel)
                .tex((float) (textureX + 0) * f, (float) (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, zLevel)
                .tex((float) (textureX + width) * f, (float) (textureY + height) * f1)
                .endVertex();
        worldrenderer.pos(x + width, y + 0, zLevel)
                .tex((float) (textureX + width) * f, (float) (textureY + 0) * f1).endVertex();
        worldrenderer.pos(x + 0, y + 0, zLevel)
                .tex((float) (textureX + 0) * f, (float) (textureY + 0) * f1).endVertex();
        tessellator.draw();
    }

    private static void drawBoundTexture(float x, float y, float u, float v, float uWidth, float vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }
    public static int reAlpha(int color, float alpha) {
        try {
            Color c = new Color(color);
            float r = ((float) 1 / 255) * c.getRed();
            float g = ((float) 1 / 255) * c.getGreen();
            float b = ((float) 1 / 255) * c.getBlue();
            return new Color(r, g, b, alpha).getRGB();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return color;
    }

    static {
        tessellator = Tessellation.createExpanding(4, 1.0f, 2.0f);
        csBuffer = new ArrayList<Integer>();
        ENABLE_CLIENT_STATE = GL11::glEnableClientState;
        DISABLE_CLIENT_STATE = GL11::glEnableClientState;
    }

    public static void drawArrow(double x, double y, int lineWidth, int color, double length) {
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        setColor(new Color(color));
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 3, y + length);
        GL11.glVertex2d(x + 3 * 2, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        stop2D();
    }
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    // animation for sliders and stuff
    public static void fakeCircleGlow(float posX, float posY, float radius, Color color, float maxAlpha) {
        setAlphaLimit(0);
        glShadeModel(GL_SMOOTH);
        GLUtil.setup2DRendering(() -> GLUtil.render(GL_TRIANGLE_FAN, () -> {
            color(color.getRGB(), maxAlpha);
            glVertex2d(posX, posY);
            color(color.getRGB(), 0);
            for (int i = 0; i <= 100; i++) {
                double angle = (i * .06283) + 3.1415;
                double x2 = Math.sin(angle) * radius;
                double y2 = Math.cos(angle) * radius;
                glVertex2d(posX + x2, posY + y2);
            }
        }));
        glShadeModel(GL_FLAT);
        setAlphaLimit(1);
    }
    public static Color brighter(Color color, float FACTOR) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();

        /* From 2D group:
         * 1. black.brighter() should return grey
         * 2. applying brighter to blue will always return blue, brighter
         * 3. non pure color (non zero rgb) will eventually return white
         */
        int i = (int) (1.0 / (1.0 - FACTOR));
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) r = i;
        if (g > 0 && g < i) g = i;
        if (b > 0 && b < i) b = i;

        return new Color(Math.min((int) (r / FACTOR), 255),
                Math.min((int) (g / FACTOR), 255),
                Math.min((int) (b / FACTOR), 255),
                alpha);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static double animate(double endPoint, double current, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        if (speed < 0.0D) {
            speed = 0.0D;
        } else if (speed > 1.0D) {
            speed = 1.0D;
        }

        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static void drawRect2(double x, double y, double width, double height, int color) {
        RenderUtil.resetColor();
        setup2DRendering(() -> render(GL11.GL_QUADS, () -> {
            RenderUtil.color(color);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x, y + height);
            GL11.glVertex2d(x + width, y + height);
            GL11.glVertex2d(x + width, y);
        }));
    }

    /**
     *
     * @param n X
     * @param n2 Y
     * @param n3 大小
     * @param n4 颜色
     * @param n5 起始点
     * @param n6 圈
     * @param n7
     */
    public static void drawArc(float n, float n2, double n3, final int n4, final int n5, final double n6, final int n7) {
        n3 *= 2.0;
        n *= 2.0f;
        n2 *= 2.0f;
        final float n8 = (n4 >> 24 & 0xFF) / 255.0f;
        final float n9 = (n4 >> 16 & 0xFF) / 255.0f;
        final float n10 = (n4 >> 8 & 0xFF) / 255.0f;
        final float n11 = (n4 & 0xFF) / 255.0f;
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glLineWidth((float)n7);
        GL11.glEnable(2848);
        GL11.glColor4f(n9, n10, n11, n8);
        GL11.glBegin(3);
        int n12 = n5;
        while (n12 <= n6) {
            GL11.glVertex2d(n + Math.sin(n12 * 3.141592653589793 / 180.0) * n3, n2 + Math.cos(n12 * 3.141592653589793 / 180.0) * n3);
            ++n12;
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius,
                           final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }
    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h,
                                  final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float var11 = (color >> 24 & 0xFF) / 255.0f;
        final float var12 = (color >> 16 & 0xFF) / 255.0f;
        final float var13 = (color >> 8 & 0xFF) / 255.0f;
        final float var14 = (color & 0xFF) / 255.0f;
        final Tessellator var15 = Tessellator.getInstance();
        final WorldRenderer var16 = var15.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        if (var11 > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float) Math.cos(i * 3.141592653589793 / 180.0) * w;
            final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static void drawCircleWithTexture(float cX, float cY, int start, int end, float radius, ResourceLocation res, int color) {
        double radian, x, y, tx, ty, xsin, ycos;
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(res);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        color(color);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = start; i < end; ++i) {
            radian = i * (Math.PI / 180.0f);
            xsin = Math.sin(radian);
            ycos = Math.cos(radian);

            x = xsin * radius;
            y = ycos * radius;

            tx = xsin * 0.5 + 0.5;
            ty = ycos * 0.5 + 0.5;

            GL11.glTexCoord2d(cX + tx, cY + ty);
            GL11.glVertex2d(cX + x, cY + y);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
    public static void drawClickGuiArrow(float x, float y, float size, Animation animation, int color) {
        glTranslatef(x, y, 0);
        setup2DRendering(() -> render(GL_TRIANGLE_STRIP, () -> {
            color(color);


            double interpolation = interpolate(0.0, size / 2.0, animation.getOutput());
            if (animation.getOutput() >= .48) {
                glVertex2d(size / 2f, interpolate(size / 2.0, 0.0, animation.getOutput()));
            }
            glVertex2d(0, interpolation);

            if (animation.getOutput() < .48) {
                glVertex2d(size / 2f, interpolate(size / 2.0, 0.0, animation.getOutput()));
            }
            glVertex2d(size, interpolation);

        }));
        glTranslatef(-x, -y, 0);
    }
    public static void render(int mode, Runnable render){
        glBegin(mode);
        render.run();
        glEnd();
    }
    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }
    public static Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public static double interpolatee(double current, double old, double scale) {
        return old + (current - old) * scale;
    }
    public static int getColor(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) 255;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }
    public static int darker(int color, float factor) {
        int r = (int)((float)(color >> 16 & 255) * factor);
        int g = (int)((float)(color >> 8 & 255) * factor);
        int b = (int)((float)(color & 255) * factor);
        int a = color >> 24 & 255;
        return (r & 255) << 16 | (g & 255) << 8 | b & 255 | (a & 255) << 24;
    }
    public static Color darker(Color color, float FACTOR) {
        return new Color(Math.max((int) (color.getRed() * FACTOR), 0),
                Math.max((int) (color.getGreen() * FACTOR), 0),
                Math.max((int) (color.getBlue() * FACTOR), 0),
                color.getAlpha());
    }
    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public static void drawGradientRect(double left, double top, double right, double bottom, boolean sideways, int startColor, int endColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        RenderUtil.color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            RenderUtil.color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            RenderUtil.color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            RenderUtil.color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    public static void drawCheckeredBackground(float x, float y, float x2, float y2) {
        RenderUtil.drawRect(x, y, x2, y2, RenderUtil.getColor(16777215));
        for(boolean offset = false; y < y2; ++y) {
            for(float x1 = x + (float)((offset = !offset) ? 1 : 0); x1 < x2; x1 += 2.0F) {
                if (x1 <= x2 - 1.0F) {
                    RenderUtil.drawRect(x1, y, x1 + 1.0F, y + 1.0F, RenderUtil.getColor(8421504));
                }
            }
        }
    }

    public static void drawStack(FontRenderer font, boolean renderOverlay, ItemStack stack, float x, float y) {
        GL11.glPushMatrix();

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld != null) {
            RenderHelper.enableGUIStandardItemLighting();
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
        GlStateManager.enableBlend();

        mc.getRenderItem().zLevel = -150.0F;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) x, (int) y);

        if (renderOverlay) {
            renderItemOverlayIntoGUI(font, stack, (int) x, (int) y, String.valueOf(stack.stackSize));
        }

        mc.getRenderItem().zLevel = 0.0F;

        GlStateManager.enableBlend();
        final float z = 0.5F;

        GlStateManager.scale(z, z, z);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(1f, 2.0f, 2.0f);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();

        GL11.glPopMatrix();
    }

    public static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
        if (stack != null) {
            if (stack.stackSize != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.stackSize) : text;
                if (text == null && stack.stackSize < 1) {
                    s = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
                }

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawString(s, (float)(xPosition + 19 - 2 - fr.stringWidth(s)), (float)(yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            if (Reflector.ForgeItem_getDurabilityForDisplay.exists()) {
                double health = Reflector.callDouble(stack.getItem(), Reflector.ForgeItem_getDurabilityForDisplay, new Object[]{stack});;
                int j = (int)Math.round(13.0 - health * 13.0);
                int i = (int)Math.round(255.0 - health * 255.0);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
                draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

    }
    public static void drawCheck(double x, double y, int lineWidth, int color) {
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        setColor(new Color(color));
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 2, y + 3);
        GL11.glVertex2d(x + 6, y - 2);
        GL11.glEnd();
        GL11.glPopMatrix();
        stop2D();
    }
    public static void setGLColor(final int color) {
        setGLColor(new Color(color));
    }

    public static void setColor(Color color) {
        float alpha = (color.getRGB() >> 24 & 0xFF) / 255.0F;
        float red = (color.getRGB() >> 16 & 0xFF) / 255.0F;
        float green = (color.getRGB() >> 8 & 0xFF) / 255.0F;
        float blue = (color.getRGB() & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
    public static void setColor(int colorHex) {
        float alpha = (float) (colorHex >> 24 & 255) / 255.0F;
        float red = (float) (colorHex >> 16 & 255) / 255.0F;
        float green = (float) (colorHex >> 8 & 255) / 255.0F;
        float blue = (float) (colorHex & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
    public static void setGLColor(final Color color) {
        final float r = color.getRed() / 255F;
        final float g = color.getGreen() / 255F;
        final float b = color.getBlue() / 255F;
        final float a = color.getAlpha() / 255F;
        glColor4f(r, g, b, a);
    }
    public static void start2D() {
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
    }

    public static void stop2D() {
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        enableTexture2D();
        disableBlend();
        glColor4f(1, 1, 1, 1);
    }
    public static void scissor(final double x, final double y, final double width, final double height) {
        int scaleFactor;
        for (scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(); scaleFactor < 2 && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320 && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240; ++scaleFactor) {}
        GL11.glScissor((int)(x * scaleFactor), (int)(Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor), (int)(width * scaleFactor), (int)(height * scaleFactor));
    }
    public RenderUtil() {
        super();
    }
    
    public static int width() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }
    public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    public static int height() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
    }
    public static void scale(float x, float y, float scale, Runnable data) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-x, -y, 0);
        data.run();
        GL11.glPopMatrix();
    }

    public static void drawRoundedRect(float x, float y, float x2, float y2, float round, int color) {
        x = (float)((double)x + (double)(round / 2.0F) + 0.5);
        y = (float)((double)y + (double)(round / 2.0F) + 0.5);
        x2 = (float)((double)x2 - ((double)(round / 2.0F) + 0.5));
        y2 = (float)((double)y2 - ((double)(round / 2.0F) + 0.5));
        drawRect(x, y, x2, y2, color);
        circle(x2 - round / 2.0F, y + round / 2.0F, round, color);
        circle(x + round / 2.0F, y2 - round / 2.0F, round, color);
        circle(x + round / 2.0F, y + round / 2.0F, round, color);
        circle(x2 - round / 2.0F, y2 - round / 2.0F, round, color);
        drawRect(x - round / 2.0F - 0.5F, y + round / 2.0F, x2, y2 - round / 2.0F, color);
        drawRect(x, y + round / 2.0F, x2 + round / 2.0F + 0.5F, y2 - round / 2.0F, color);
        drawRect(x + round / 2.0F, y - round / 2.0F - 0.5F, x2 - round / 2.0F, y2 - round / 2.0F, color);
        drawRect(x + round / 2.0F, y, x2 - round / 2.0F, y2 + round / 2.0F + 0.5F, color);
    }

    public static void circle(float x, float y, float radius, int fill) {
        GL11.glEnable(3042);
        arc(x, y, 0.0F, 360.0F, radius, fill);
        GL11.glDisable(3042);
    }


    public static int getHexRGB(final int hex) {
        return 0xFF000000 | hex;
    }
    public static void drawRoundedRect(float x, float y, float width, float height, float edgeRadius, int color, float borderWidth, int borderColor) {
        if (color == 16777215) color = -65794;
        if (borderColor == 16777215) borderColor = -65794;

        if (edgeRadius < 0.0F) {
            edgeRadius = 0.0F;
        }

        if (edgeRadius > width / 2.0F) {
            edgeRadius = width / 2.0F;
        }

        if (edgeRadius > height / 2.0F) {
            edgeRadius = height / 2.0F;
        }

        drawRDRect(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0F, height - edgeRadius * 2.0F, color);
        drawRDRect(x + edgeRadius, y, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRDRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRDRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        drawRDRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        enableRender2D();
        color(color);
        GL11.glBegin(6);
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        int vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        int i;
        double angleRadians;
        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);

        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        color(borderColor);
        GL11.glLineWidth(borderWidth);
        GL11.glBegin(3);
        centerX = x + edgeRadius;
        centerY = y + edgeRadius;
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + edgeRadius), y);
        GL11.glVertex2d((x + width - edgeRadius), y);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + width), (y + edgeRadius));
        GL11.glVertex2d((x + width), (y + height - edgeRadius));
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + width - edgeRadius), (y + height));
        GL11.glVertex2d((x + edgeRadius), (y + height));

        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x, y + height - edgeRadius);
        GL11.glVertex2d(x, y + edgeRadius);
        GL11.glEnd();
        disableRender2D();
    }
    public static void disableRender2D() {
        GL11.glDisable(3042);
        GL11.glEnable(2884);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
    public static void drawRDRect(float left, float top, float width, float height, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
    }
    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, (float) 0, (float) 0, (int) width, (int) height, width, height);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, float alpha) {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, width, height);
        GL11.glDepthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.resetColor();
    }


    public static void enableRender2D() {
        GL11.glEnable(3042);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0f);
    }
    public static void drawCustomImage(int x, int y, int width, int height, ResourceLocation image) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDepthMask((boolean)false);
        OpenGlHelper.glBlendFunc((int)770, (int)771, (int)1, (int)0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, (float)0.0f, (float)0.0f, (int)width, (int)height, (float)width, (float)height);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
    }
    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, int color) {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        GL11.glColor4f(f1, f2, f3, f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height, width, height);
        GL11.glDepthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.resetColor();
    }
    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)x, (double)(y + height), 0.0).tex((double)(u * f), (double)((v + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0).tex((double)((u + width) * f), (double)((v + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)y, 0.0).tex((double)((u + width) * f), (double)(v * f1)).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
    }


    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static void drawRect(final float x, final float y, final float x2, final float y2, final Color color) {
        drawRect(x, y, x2, y2, color.getRGB());
    }
    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBorderedRect(final float x, final float y, final float x2, final float y2, final float l1, final int col1, final int col2) {
        drawRect(x, y, x2, y2, col2);
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(l1);
        GL11.glBegin(1);
        GL11.glVertex2d((double)x, (double)y);
        GL11.glVertex2d((double)x, (double)y2);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glVertex2d((double)x2, (double)y);
        GL11.glVertex2d((double)x, (double)y);
        GL11.glVertex2d((double)x2, (double)y);
        GL11.glVertex2d((double)x, (double)y2);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }
    
    public static void pre() {
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
    }
    
    public static void post() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glColor3d(1.0, 1.0, 1.0);
    }
    
    public static void startDrawing() {
        GL11.glEnable(3042);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
//        Helper.mc.entityRenderer.setupCameraTransform(Helper.mc.timer.renderPartialTicks, 0);
    }
    
    public static void stopDrawing() {
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }
    
    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float)ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        final Color color3 = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
        return color3;
    }
    
    public static void drawLine(final Vec2f start, final Vec2f end, final float width) {
        drawLine(start.getX(), start.getY(), end.getX(), end.getY(), width);
    }
    
    public static void drawLine(final Vec3f start, final Vec3f end, final float width) {
        drawLine((float)start.getX(), (float)start.getY(), (float)start.getZ(), (float)end.getX(), (float)end.getY(), (float)end.getZ(), width);
    }

    public static void drawLine(final float x, final float y, final float x1, final float y1, final float width) {
        drawLine(x, y, 0.0f, x1, y1, 0.0f, width);
    }

    public static void drawLine(final float x, final float y, final float z, final float x1, final float y1, final float z1, final float width) {
        GL11.glLineWidth(width);
        setupRender(true);
        setupClientState(GLClientState.VERTEX, true);
        RenderUtil.tessellator.addVertex(x, y, z).addVertex(x1, y1, z1).draw(3);
        setupClientState(GLClientState.VERTEX, false);
        setupRender(false);
    }

    public static void setupClientState(final GLClientState state, final boolean enabled) {
        RenderUtil.csBuffer.clear();
        if (state.ordinal() > 0) {
            RenderUtil.csBuffer.add(state.getCap());
        }
        RenderUtil.csBuffer.add(32884);
        RenderUtil.csBuffer.forEach(enabled ? RenderUtil.ENABLE_CLIENT_STATE : RenderUtil.DISABLE_CLIENT_STATE);
    }

    // Sometimes colors get messed up in for loops, so we use this method to reset it to allow new colors to be used
    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }
    // This method colors the next avalible texture with a specified alpha value ranging from 0-1
    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }
    public static void setupRender(final boolean start) {
        if (start) {
            GlStateManager.enableBlend();
            GL11.glEnable(2848);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GL11.glHint(3154, 4354);
        }
        else {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GL11.glDisable(2848);
            GlStateManager.enableDepth();
        }
        GlStateManager.depthMask(!start);
    }
    // Colors the next texture without a specified alpha value
    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }

    /**
     * Bind a texture using the specified integer refrence to the texture.
     *
     * @see org.lwjgl.opengl.GL13 for more information about texture bindings
     */
    public static void bindTexture(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
    }
}
