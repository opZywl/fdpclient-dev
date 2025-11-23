package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl

import net.minecraft.client.renderer.GlStateManager

object GLUtils {
    fun init() {
    }

    fun getColor(hex: Int): FloatArray {
        return floatArrayOf(
            (hex shr 16 and 255) / 255.0f,
            (hex shr 8 and 255) / 255.0f,
            (hex and 255) / 255.0f,
            (hex shr 24 and 255) / 255.0f
        )
    }

    fun glColor(hex: Int) {
        val color = getColor(hex)
        GlStateManager.color(color[0], color[1], color[2], color[3])
    }
}
