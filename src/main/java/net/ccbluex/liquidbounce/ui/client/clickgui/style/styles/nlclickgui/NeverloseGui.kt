package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import com.mojang.realmsclient.gui.ChatFormatting
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.Configs
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.NeverloseConfigManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.ColorSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.StringsSetting
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.BloomUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.GaussianBlur
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import javax.imageio.ImageIO

class NeverloseGui : GuiScreen() {

    var x = 0
    var y = 0
    var w = 0
    var h = 0

    lateinit var alphaani: Animation

    var subCategory: Category.SubCategory? = null

    val nlTabs: MutableList<NlTab> = ArrayList()

    var loader = true

    private var x2 = 0
    private var y2 = 0

    private var dragging = false
    private var settings = false
    private var search = false
    private var head = true

    private val nlSetting = NlSetting()

    private val searchanim: Animation = EaseInOutQuad(400.0, 1.0, Direction.BACKWARDS)

    var configs = Configs()
    private val configManager = NeverloseConfigManager()

    private var bloomFramebuffer = Framebuffer(1, 1, false)

    init {
        INSTANCE = this
        x = 100
        y = 100
        w = 430
        h = 300

        var y2 = 0
        var u2 = 0
        for (type in Category.values()) {
            if (type.name.equals("World", ignoreCase = true) ||
                type.name.equals("Interface", ignoreCase = true)
            ) continue

            nlTabs.add(NlTab(type, u2 + y2 + 40))

            for (subCategory in type.subCategories) {
                u2 += 17
            }

            y2 += 14
        }

        if (head) {
            try {
                Minecraft.getMinecraft().textureManager.loadTexture(
                    ResourceLocation("nb"),
                    DynamicTexture(ImageIO.read(URL("https://q.qlogo.cn/headimg_dl?dst_uin=" + "2165728490" + "&spec=100"))))
                head = false
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun initGui() {
        super.initGui()
        configManager.refresh()

        alphaani = EaseInOutQuad(300.0, 0.6, Direction.FORWARDS)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GL11.glPushMatrix()

        if (loader && nlTabs.isNotEmpty()) {
            subCategory = nlTabs[0].nlSubList[0].subCategory
            loader = false
        }

        if (dragging) {
            x = x2 + mouseX
            y = y2 + mouseY
        }

        bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer)

        bloomFramebuffer.framebufferClear()
        bloomFramebuffer.bindFramebuffer(true)
        RoundedUtil.drawRound(x, y, w, h, 4f, if (nlSetting.Light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        bloomFramebuffer.unbindFramebuffer()

        BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, 6, 3)

        StencilUtil.initStencilToWrite()
        RoundedUtil.drawRound(x, y, w, h, 4f, if (nlSetting.Light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        StencilUtil.readStencilBuffer(1)
        GaussianBlur.renderBlur(10)
        StencilUtil.uninitStencilBuffer()

        RoundedUtil.drawRound(x, y, w, h, 2f, if (nlSetting.Light) Color(240, 245, 248, 230) else Color(7, 13, 23, 230))
        RoundedUtil.drawRound(x + 90, y + 40, w - 90, h - 40, 1f, if (nlSetting.Light) Color(255, 255, 255) else Color(9, 9, 9))
        RoundedUtil.drawRound(x + 90, y, w - 90, h - 260, 1f, if (nlSetting.Light) Color(255, 255, 255) else Color(13, 13, 11))

        RoundedUtil.drawRound(x + 90, y + 39, w - 90, 1f, 0f, if (nlSetting.Light) Color(213, 213, 213) else Color(26, 26, 26))
        RoundedUtil.drawRound(x + 89, y, 1, h, 0f, if (nlSetting.Light) Color(213, 213, 213) else Color(26, 26, 26))

        GL11.glEnable(GL11.GL_BLEND)

        mc.textureManager.bindTexture(ResourceLocation("nb"))

        RoundedUtil.drawRoundTextured(x + 4, y + 274, 20, 20, 10f, 1f)

        Fonts.Nl_18.drawString(mc.session.username, x + 29, y + 275, if (nlSetting.Light) Color(51, 51, 51).rgb else -1)

        Fonts.Nl_16.drawString(
            ChatFormatting.GRAY.toString() + "Till: " + ChatFormatting.RESET + SimpleDateFormat("dd:MM").format(Date()) + " " + SimpleDateFormat(
                "HH:mm"
            ).format(Date()),
            x + 29,
            y + 287,
            neverlosecolor.rgb
        )

        if (!nlSetting.Light) {
            NLOutline("FDPCLIENT", Fonts.NLBold_28, (x + 7).toFloat(), (y + 12).toFloat(), -1, neverlosecolor.rgb, 0.7f)
        } else {
            Fonts.NLBold_28.drawString("FDP", (x + 8).toFloat(), (y + 12).toFloat(), Color(51, 51, 51).rgb, false)
        }

        RoundedUtil.drawRound(x, y + 265, 89, 1, 0f, if (nlSetting.Light) Color(213, 213, 213) else Color(26, 26, 26))

        for (nlTab in nlTabs) {
            nlTab.x = x
            nlTab.y = y
            nlTab.w = w
            nlTab.h = h

            nlTab.draw(mouseX, mouseY)
        }

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString(
            "x",
            (x + w - 50 + if (search || !searchanim.isDone) (-83 * searchanim.output).toFloat() else 0f).toFloat(),
            (y + 17).toFloat(),
            if (settings) neverlosecolor.rgb else if (INSTANCE.light) Color(95, 95, 95).rgb else -1
        )

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString(
            "j",
            (x + w - 30).toFloat(),
            (y + 18).toFloat(),
            if (search) neverlosecolor.rgb else if (INSTANCE.light) Color(95, 95, 95).rgb else -1
        )

        searchanim.setDirection(if (search) Direction.FORWARDS else Direction.BACKWARDS)

        if (search || !searchanim.isDone) {
            RenderUtil.drawRoundedRect(
                (x + w - 30 - 85 * searchanim.output).toFloat(),
                (y + 12).toFloat(),
                (80 * searchanim.output).toFloat(),
                15f,
                1,
                Color(3, 13, 26).rgb,
                1,
                if (INSTANCE.light) Color(95, 95, 95).rgb else Color(28, 133, 192).rgb
            )
        }

        if (settings) {
            nlSetting.draw(mouseX, mouseY)
        }

        RoundedUtil.drawRoundOutline(
            x + 105,
            y + 10,
            55,
            21,
            2f,
            0.1f,
            if (INSTANCE.light) Color(245, 245, 245) else Color(13, 13, 11),
            if (RenderUtil.isHovering(x + 105, y + 10, 55, 21, mouseX, mouseY)) neverlosecolor else Color(19, 19, 17)
        )

        Fonts.Nl_18.drawString("Save", (x + 128).toFloat(), (y + 18).toFloat(), if (INSTANCE.light) Color(18, 18, 19).rgb else -1)

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString("K", (x + 110).toFloat(), (y + 19).toFloat(), if (INSTANCE.light) Color(18, 18, 19).rgb else -1)

        GL11.glPopMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        nlTabs.forEach { it.click(mouseX, mouseY, mouseButton) }
        if (settings) {
            nlSetting.click(mouseX, mouseY, mouseButton)
        }
        if (mouseButton == 0) {
            if (RenderUtil.isHovering(x + 110, y, w - 110, h - 260, mouseX, mouseY)) {
                x2 = (x - mouseX)
                y2 = (y - mouseY)
                dragging = true
            }

            if (RenderUtil.isHovering(x + 105, y + 10, 55, 21, mouseX, mouseY)) {
                if (configManager.activeConfig != null) {
                    configManager.saveConfig(configManager.activeConfig!!.name)
                } else {
                    FDPClient.fileManager.saveAllConfigs()
                    configManager.refresh()
                }
            }

            if (RenderUtil.isHovering(
                    (x + w - 50 + if (search || !searchanim.isDone) -83 * searchanim.output else 0.0).toFloat(),
                    (y + 17).toFloat(),
                    Fonts.NlIcon.nlfont_24.getNlfont_24().stringWidth("x"),
                    Fonts.NlIcon.nlfont_24.getNlfont_24().height,
                    mouseX,
                    mouseY
                )
            ) {
                settings = !settings
                dragging = false
                nlSetting.x = x + w + 20
                nlSetting.y = y
            }

            if (RenderUtil.isHovering(
                    (x + w - 30).toFloat(),
                    (y + 18).toFloat(),
                    Fonts.NlIcon.nlfont_20.getNlfont_20().stringWidth("j"),
                    Fonts.NlIcon.nlfont_20.getNlfont_20().height,
                    mouseX,
                    mouseY
                )
            ) {
                search = !search
                dragging = false
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        nlTabs.forEach { it.released(mouseX, mouseY, state) }

        if (state == 0) {
            dragging = false
        }

        if (settings) {
            nlSetting.released(mouseX, mouseY, state)
        }

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        nlTabs.forEach { it.keyTyped(typedChar, keyCode) }
        super.keyTyped(typedChar, keyCode)
    }

    val light: Boolean
        get() = nlSetting.Light

    companion object {
        @JvmStatic
        lateinit var INSTANCE: NeverloseGui

        @JvmStatic
        val neverlosecolor: Color = Color(28, 133, 192)

        @JvmStatic
        fun getInstance(): NeverloseGui = INSTANCE

        @JvmStatic
        fun NLOutline(str: String, fontRenderer: FontRenderer, x: Float, y: Float, color: Int, color2: Int, size: Float) {
            fontRenderer.drawString(str, x + size, y, color2, false)
            fontRenderer.drawString(str, x, y - size, color2, false)
            fontRenderer.drawString(str, x, y, color, false)
        }
    }
}
