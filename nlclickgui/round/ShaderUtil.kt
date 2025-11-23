package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.*

class ShaderUtil(fragmentShaderLoc: String, vertexShaderLoc: String = "fdpclient/shaders/vertex.vert") {
    private val programID: Int

    init {
        val program = glCreateProgram()
        val fragmentShaderID: Int
        try {
            fragmentShaderID = createShader(fragmentShaderLoc, GL_FRAGMENT_SHADER)
        } catch (e: Exception) {
            throw IllegalStateException("Failed creating fragment shader", e)
        }
        val vertexShaderID: Int
        try {
            vertexShaderID = createShader(vertexShaderLoc, GL_VERTEX_SHADER)
        } catch (e: Exception) {
            glDeleteShader(fragmentShaderID)
            throw IllegalStateException("Failed creating vertex shader", e)
        }
        glAttachShader(program, fragmentShaderID)
        glAttachShader(program, vertexShaderID)
        glLinkProgram(program)
        glValidateProgram(program)
        glDeleteShader(fragmentShaderID)
        glDeleteShader(vertexShaderID)
        programID = program
    }

    fun init() {
        glUseProgram(programID)
    }

    fun unload() {
        glUseProgram(0)
    }

    fun setUniformf(location: String, v0: Float, v1: Float) {
        glUniform2f(getUniform(location), v0, v1)
    }

    fun setUniformf(location: String, v0: Float, v1: Float, v2: Float) {
        glUniform3f(getUniform(location), v0, v1, v2)
    }

    fun setUniformf(location: String, v0: Float, v1: Float, v2: Float, v3: Float) {
        glUniform4f(getUniform(location), v0, v1, v2, v3)
    }

    fun setUniformi(location: String, v0: Int) {
        glUniform1i(getUniform(location), v0)
    }

    fun setUniformi(location: String, v0: Int, v1: Int) {
        glUniform2i(getUniform(location), v0, v1)
    }

    fun setUniformi(location: String, v0: Int, v1: Int, v2: Int) {
        glUniform3i(getUniform(location), v0, v1, v2)
    }

    fun setUniformi(location: String, v0: Int, v1: Int, v2: Int, v3: Int) {
        glUniform4i(getUniform(location), v0, v1, v2, v3)
    }

    fun setUniform(location: String, `val`: Boolean) {
        glUniform1i(getUniform(location), if (`val`) 1 else 0)
    }

    private fun getUniform(name: String): Int {
        return glGetUniformLocation(programID, name)
    }

    private fun createShader(location: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, readShaderFile(location))
        glCompileShader(shader)
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            val error = glGetShaderInfoLog(shader, 2048)
            glDeleteShader(shader)
            throw IllegalStateException("Failed to compile shader: $error")
        }
        return shader
    }

    private fun readShaderFile(location: String): String {
        val inputStream = Minecraft.getMinecraft().resourceManager
            .getResource(ResourceLocation(location)).inputStream
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            val shaderSource = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                shaderSource.append(line).append('\n')
            }
            return shaderSource.toString()
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()

        fun drawQuads(x: Float, y: Float, width: Float, height: Float) {
            glBegin(GL_QUADS)
            glTexCoord2f(0f, 1f)
            glVertex2f(x, y)
            glTexCoord2f(0f, 0f)
            glVertex2f(x, y + height)
            glTexCoord2f(1f, 0f)
            glVertex2f(x + width, y + height)
            glTexCoord2f(1f, 1f)
            glVertex2f(x + width, y)
            glEnd()
        }

        fun drawQuads(x: Double, y: Double, width: Double, height: Double) {
            drawQuads(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        }

        fun drawQuads(x: Int, y: Int, width: Int, height: Int) {
            drawQuads(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        }

        fun setupRoundedRectUniforms(x: Float, y: Float, width: Float, height: Float, radius: Float, shader: ShaderUtil) {
            val sr = ScaledResolution(mc)
            shader.setUniformf("location", x * sr.scaleFactor, (mc.displayHeight - height * sr.scaleFactor) - y * sr.scaleFactor)
            shader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
            shader.setUniformf("radius", radius * sr.scaleFactor)
        }
    }
}
