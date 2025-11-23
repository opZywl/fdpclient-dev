package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.*

class ShaderUtil @JvmOverloads constructor(
    fragmentShaderLoc: String,
    vertexShaderLoc: String = "fdpclient/shaders/vertex.vsh"
) {
    private val programID: Int

    fun init() {
        GL20.glUseProgram(programID)
    }

    fun unload() {
        GL20.glUseProgram(0)
    }

    fun getUniform(name: String): Int {
        return GL20.glGetUniformLocation(programID, name)
    }


    fun setUniformf(name: String, vararg args: Float) {
        val loc = GL20.glGetUniformLocation(programID, name)
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String, vararg args: Int) {
        val loc = GL20.glGetUniformLocation(programID, name)
        if (args.size > 1) GL20.glUniform2i(loc, args[0], args[1])
        else GL20.glUniform1i(loc, args[0])
    }

    private fun createShader(inputStream: InputStream, shaderType: Int): Int {
        val shader = GL20.glCreateShader(shaderType)
        GL20.glShaderSource(shader, readInputStream(inputStream))
        GL20.glCompileShader(shader)


        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            println(GL20.glGetShaderInfoLog(shader, 4096))
            throw IllegalStateException(String.format("Shader (%s) failed to compile!", shaderType))
        }

        return shader
    }

    private val roundedRectGradient = "#version 120\" +
            "\" +
            "uniform vec2 location, rectSize;\" +
            "uniform vec4 color1, color2, color3, color4;\" +
            "uniform float radius;\" +
            "\" +
            "#define NOISE .5/255.0\" +
            "\" +
            "float roundSDF(vec2 p, vec2 b, float r) {\" +
            "    return length(max(abs(p) - b , 0.0)) - r;\" +
            "}\" +
            "\" +
            "vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){\" +
            "    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);\" +
            "    color += mix(NOISE, -NOISE, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));\" +
            "    return color;\" +
            "}\" +
            "\" +
            "void main() {\" +
            "    vec2 st = gl_TexCoord[0].st;\" +
            "    vec2 halfSize = rectSize * .5;\" +
            "    \" +
            "    float smoothedAlpha =  (1.0-smoothstep(0.0, 2., roundSDF(halfSize - (gl_TexCoord[0].st * rectSize), halfSize - radius - 1., radius))) * color1.a;\" +
            "    gl_FragColor = vec4(createGradient(st, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);\" +
            "}"


    private val roundedRect = "#version 120\" +
            "\" +
            "uniform vec2 location, rectSize;\" +
            "uniform vec4 color;\" +
            "uniform float radius;\" +
            "uniform bool blur;\" +
            "\" +
            "float roundSDF(vec2 p, vec2 b, float r) {\" +
            "    return length(max(abs(p) - b, 0.0)) - r;\" +
            "}\" +
            "\" +
            "\" +
            "void main() {\" +
            "    vec2 rectHalf = rectSize * .5;\" +
            "    float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;\" +
            "    gl_FragColor = vec4(color.rgb, smoothedAlpha);\" +
            "\" +
            "}"

    init {
        val program = GL20.glCreateProgram()
        try {
            val fragmentShaderID: Int
            when (fragmentShaderLoc) {
                "roundedRect" -> fragmentShaderID =
                    createShader(ByteArrayInputStream(roundedRect.toByteArray()), GL20.GL_FRAGMENT_SHADER)

                "roundedRectGradient" -> fragmentShaderID =
                    createShader(ByteArrayInputStream(roundedRectGradient.toByteArray()), GL20.GL_FRAGMENT_SHADER)

                else -> fragmentShaderID = createShader(
                    mc.getResourceManager().getResource(ResourceLocation(fragmentShaderLoc)).getInputStream(),
                    GL20.GL_FRAGMENT_SHADER
                )
            }
            GL20.glAttachShader(program, fragmentShaderID)

            val vertexShaderID = createShader(
                mc.getResourceManager().getResource(ResourceLocation(vertexShaderLoc)).getInputStream(),
                GL20.GL_VERTEX_SHADER
            )
            GL20.glAttachShader(program, vertexShaderID)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        GL20.glLinkProgram(program)
        val status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS)

        check(status != 0) { "Shader failed to link!" }
        this.programID = program
    }


    companion object {
        var mc: Minecraft = Minecraft.getMinecraft()
        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(x, y)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(x, y + height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(x + width, y + height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(x + width, y)
            GL11.glEnd()
        }

        fun drawQuads() {
            val sr = ScaledResolution(mc)
            val width = sr.getScaledWidth_double().toFloat()
            val height = sr.getScaledHeight_double().toFloat()
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2f(0f, 1f)
            GL11.glVertex2f(0f, 0f)
            GL11.glTexCoord2f(0f, 0f)
            GL11.glVertex2f(0f, height)
            GL11.glTexCoord2f(1f, 0f)
            GL11.glVertex2f(width, height)
            GL11.glTexCoord2f(1f, 1f)
            GL11.glVertex2f(width, 0f)
            GL11.glEnd()
        }

        fun readInputStream(inputStream: InputStream): String {
            val stringBuilder = StringBuilder()

            try {
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while ((bufferedReader.readLine().also { line = it }) != null) stringBuilder.append(line).append('\')
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return stringBuilder.toString()
        }
    }
}
