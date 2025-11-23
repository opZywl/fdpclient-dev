/*
 * Decompiled with CFR 0_132.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.util.glu.GLU
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl;

import net.minecraft.client.renderer.GlStateManager;

public final class GLUtils {
    private GLUtils() {
    }

    public static void init() {
    }

    public static float[] getColor(int hex) {
        return new float[]{(float)(hex >> 16 & 255) / 255.0f, (float)(hex >> 8 & 255) / 255.0f, (float)(hex & 255) / 255.0f, (float)(hex >> 24 & 255) / 255.0f};
    }

    public static void glColor(int hex) {
        float[] color = GLUtils.getColor(hex);
        GlStateManager.color(color[0], color[1], color[2], color[3]);
    }
}