package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils.interpolateInt
import net.ccbluex.liquidbounce.utils.extensions.interpolateFloat
import net.minecraft.util.MathHelper
import java.awt.Color
import java.util.regex.Pattern

object ColorUtils {
    private val colorPattern: Pattern = Pattern.compile("(?i)§[0-9A-FK-OR]")

    @JvmStatic
    fun randomColor(): Int = -0x1000000 or (Math.random() * 1.6777215E7).toInt()

    @JvmStatic
    fun getColor(brightness: Int): Int = getColor(brightness, brightness, brightness, 255)

    @JvmStatic
    fun getColor(red: Int, green: Int, blue: Int): Int = getColor(red, green, blue, 255)

    @JvmStatic
    fun fade(color: Color): Color = fade(color, 2, 100)

    @JvmStatic
    fun fade(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness = kotlin.math.abs((System.currentTimeMillis() % 2000L).toFloat() / 1000f + index.toFloat() / count.toFloat() * 2f) % 2f - 1f
        brightness = 0.5f + 0.5f * brightness
        hsb[2] = brightness % 2f
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }

    @JvmStatic
    fun getColor(brightness: Int, alpha: Int): Int = getColor(brightness, brightness, brightness, alpha)

    @JvmStatic
    fun getColor(red: Int, green: Int, blue: Int, alpha: Int): Int {
        var color = MathHelper.clamp_int(alpha, 0, 255) shl 24
        color = color or (MathHelper.clamp_int(red, 0, 255) shl 16)
        color = color or (MathHelper.clamp_int(green, 0, 255) shl 8)
        color = color or MathHelper.clamp_int(blue, 0, 255)
        return color
    }

    @JvmStatic
    fun transparency(color: Int, alpha: Double): Int {
        val c = Color(color)
        val r = 0.003921569f * c.red.toFloat()
        val g = 0.003921569f * c.green.toFloat()
        val b = 0.003921569f * c.blue.toFloat()
        return Color(r, g, b, alpha.toFloat()).rgb
    }

    @JvmStatic
    fun stripColor(input: String): String = colorPattern.matcher(if (input == "") null else input).replaceAll("")

    @JvmStatic
    fun applyOpacity(color: Int, opacity: Float): Int = applyOpacity(Color(color), opacity).rgb

    @JvmStatic
    fun applyOpacity(color: Color, opacity: Float): Color {
        val clamped = opacity.coerceIn(0f, 1f)
        return Color(color.red, color.green, color.blue, (color.alpha * clamped).toInt())
    }

    @JvmStatic
    fun transparency(color: Color, alpha: Double): Int = Color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), alpha.toFloat()).rgb

    @JvmStatic
    fun interpolateColorsBackAndForth(speed: Int, index: Int, start: Color, end: Color, trueColor: Boolean): Color {
        var angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        angle = (if (angle >= 180) 360 - angle else angle) * 2
        val fraction = angle / 360f
        return if (trueColor) interpolateColorHue(start, end, fraction) else interpolateColorC(start, end, fraction)
    }

    @JvmStatic
    fun rainbow(speed: Int, index: Int, saturation: Float, brightness: Float, opacity: Float): Color {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val hue = angle / 360f
        val color = Color(Color.HSBtoRGB(hue, saturation, brightness))
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        return Color(color.red, color.green, color.blue, alpha)
    }

    @JvmStatic
    fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
        val amt = amount.coerceIn(0f, 1f)
        return Color(
            interpolateInt(color1.red, color2.red, amt),
            interpolateInt(color1.green, color2.green, amt),
            interpolateInt(color1.blue, color2.blue, amt),
            interpolateInt(color1.alpha, color2.alpha, amt)
        )
    }

    @JvmStatic
    fun interpolateColorHue(color1: Color, color2: Color, amount: Float): Color {
        val amt = amount.coerceIn(0f, 1f)
        val color1HSB = Color.RGBtoHSB(color1.red, color1.green, color1.blue, null)
        val color2HSB = Color.RGBtoHSB(color2.red, color2.green, color2.blue, null)

        val resultColor = Color.getHSBColor(
            interpolateFloat(color1HSB[0], color2HSB[0], amt),
            interpolateFloat(color1HSB[1], color2HSB[1], amt),
            interpolateFloat(color1HSB[2], color2HSB[2], amt)
        )
        return Color(resultColor.red, resultColor.green, resultColor.blue, interpolateInt(color1.alpha, color2.alpha, amt))
    }

    @JvmStatic
    fun rainbow(offset: Long, fade: Float): Color {
        val hue = ((System.nanoTime() + offset) / 1.0E10f % 1.0f)
        val color = java.lang.Long.parseLong(Integer.toHexString(Color.HSBtoRGB(hue, 1.0f, 1.0f)), 16)
        val c = Color(color.toInt())
        return Color(c.red / 255.0f * fade, c.green / 255.0f * fade, c.blue / 255.0f * fade, c.alpha / 255.0f)
    }

    @JvmStatic
    fun getRGBA(color: Int): FloatArray {
        val a = (color shr 24 and 255) / 255.0f
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f
        return floatArrayOf(r, g, b, a)
    }

    @JvmStatic
    fun intFromHex(hex: String): Int = try {
        if (hex.equals("rainbow", ignoreCase = true)) {
            rainbow(0L, 1.0f).rgb
        } else {
            Integer.parseInt(hex, 16)
        }
    } catch (_: NumberFormatException) {
        -1
    }

    @JvmStatic
    fun hexFromInt(color: Int): String = hexFromInt(Color(color))

    @JvmStatic
    fun hexFromInt(color: Color): String = Integer.toHexString(color.rgb).substring(2)

    @JvmStatic
    fun blend(color1: Color, color2: Color, ratio: Double): Color {
        val r = ratio.toFloat()
        val ir = 1.0f - r
        val rgb1 = FloatArray(3)
        val rgb2 = FloatArray(3)
        color1.getColorComponents(rgb1)
        color2.getColorComponents(rgb2)
        return Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir)
    }

    @JvmStatic
    fun blend(color1: Color, color2: Color): Color = blend(color1, color2, 0.5)

    @JvmStatic
    fun darker(color: Color, fraction: Double): Color {
        var red = (color.red.toDouble() * (1.0 - fraction)).roundToChannel()
        var green = (color.green.toDouble() * (1.0 - fraction)).roundToChannel()
        var blue = (color.blue.toDouble() * (1.0 - fraction)).roundToChannel()
        val alpha = color.alpha
        red = red.coerceIn(0, 255)
        green = green.coerceIn(0, 255)
        blue = blue.coerceIn(0, 255)
        return Color(red, green, blue, alpha)
    }

    @JvmStatic
    fun lighter(color: Color, fraction: Double): Color {
        var red = (color.red.toDouble() * (1.0 + fraction)).roundToChannel()
        var green = (color.green.toDouble() * (1.0 + fraction)).roundToChannel()
        var blue = (color.blue.toDouble() * (1.0 + fraction)).roundToChannel()
        val alpha = color.alpha
        red = red.coerceIn(0, 255)
        green = green.coerceIn(0, 255)
        blue = blue.coerceIn(0, 255)
        return Color(red, green, blue, alpha)
    }

    @JvmStatic
    fun getHexName(color: Color): String {
        val rHex = Integer.toString(color.red, 16)
        val gHex = Integer.toString(color.green, 16)
        val bHex = Integer.toString(color.blue, 16)
        val rString = if (rHex.length == 2) rHex else "0$rHex"
        val gString = if (gHex.length == 2) gHex else "0$gHex"
        val bString = if (bHex.length == 2) bHex else "0$bHex"
        return rString + gString + bString
    }

    @JvmStatic
    fun colorDistance(r1: Double, g1: Double, b1: Double, r2: Double, g2: Double, b2: Double): Double {
        val a = r2 - r1
        val b = g2 - g1
        val c = b2 - b1
        return Math.sqrt(a * a + b * b + c * c)
    }

    @JvmStatic
    fun colorDistance(color1: DoubleArray, color2: DoubleArray): Double = colorDistance(color1[0], color1[1], color1[2], color2[0], color2[1], color2[2])

    @JvmStatic
    fun colorDistance(color1: Color, color2: Color): Double {
        val rgb1 = FloatArray(3)
        val rgb2 = FloatArray(3)
        color1.getColorComponents(rgb1)
        color2.getColorComponents(rgb2)
        return colorDistance(rgb1[0].toDouble(), rgb1[1].toDouble(), rgb1[2].toDouble(), rgb2[0].toDouble(), rgb2[1].toDouble(), rgb2[2].toDouble())
    }

    @JvmStatic
    fun isDark(r: Double, g: Double, b: Double): Boolean {
        val dWhite = colorDistance(r, g, b, 1.0, 1.0, 1.0)
        val dBlack = colorDistance(r, g, b, 0.0, 0.0, 0.0)
        return dBlack < dWhite
    }

    @JvmStatic
    fun isDark(color: Color): Boolean {
        val r = color.red.toFloat() / 255.0f
        val g = color.green.toFloat() / 255.0f
        val b = color.blue.toFloat() / 255.0f
        return isDark(r.toDouble(), g.toDouble(), b.toDouble())
    }

    private fun Double.roundToChannel(): Int = Math.round(this).toInt()
}
