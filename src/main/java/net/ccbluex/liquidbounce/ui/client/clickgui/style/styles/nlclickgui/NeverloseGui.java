package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.Configs;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config.NeverloseConfigManager;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.ColorSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.StringsSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.BloomUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur.GaussianBlur;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NeverloseGui extends GuiScreen {

    public static NeverloseGui INSTANCE;

    public int x,y,w,h;

    public Animation alphaani;

    public static Color neverlosecolor = new Color(28,133,192);

    public Category.SubCategory subCategory = null;

    public List<NlTab> nlTabs = new ArrayList<>();

    public boolean Loader = true;

    private int x2;
    private int y2;

    private boolean dragging,settings,search;
    private boolean head = true;

    private NlSetting nlSetting;

    private Animation searchanim = new EaseInOutQuad(400,1, Direction.BACKWARDS);


    public Configs configs = new Configs();
    private final NeverloseConfigManager configManager = new NeverloseConfigManager();

    public NeverloseGui(){
        INSTANCE = this;
        x = 100;
        y = 100;
        w = 430;
        h = 300;

        int y2 = 0;
        int u2 =0;
        for (Category type : Category.values()){
            if(type.name().equalsIgnoreCase("World") ||
                    type.name().equalsIgnoreCase("Interface")) continue;

            nlTabs.add(new NlTab(type,u2 + y2 + 40));

            for (Category.SubCategory subCategory: type.getSubCategories()){
                u2 += 17;
            }

            y2 += 14;
        }

        nlSetting = new NlSetting();

        if (head) {
            try {
                Minecraft.getMinecraft().getTextureManager().loadTexture(
                        new ResourceLocation("nb"),
                        new DynamicTexture(ImageIO.read(new URL("https://q.qlogo.cn/headimg_dl?dst_uin="+"2165728490"+"&spec=100"))));
                head =false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    @Override
    public void initGui() {
        super.initGui();
        configManager.refresh();

        alphaani = new EaseInOutQuad(300,0.6,Direction.FORWARDS);
    }

    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GL11.glPushMatrix();





        if (Loader && !nlTabs.isEmpty()){
            subCategory = nlTabs.get(0).nlSubList.get(0).subCategory;
            Loader = false;
        }

        if(dragging) {
            this.x = x2 + mouseX;
            this.y = y2 + mouseY;
        }

        bloomFramebuffer = RenderUtil.createFrameBuffer(bloomFramebuffer);

        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);
        RoundedUtil.drawRound(x,y,w,h,4,nlSetting.Light ? new Color(240,245,248,230) : new Color(7,13,23,230));
        bloomFramebuffer.unbindFramebuffer();

        BloomUtil.renderBlur(bloomFramebuffer.framebufferTexture, 6, 3);

        StencilUtil.initStencilToWrite();
        RoundedUtil.drawRound(x,y,w,h,4,nlSetting.Light ? new Color(240,245,248,230) : new Color(7,13,23,230));
        StencilUtil.readStencilBuffer(1);
        GaussianBlur.renderBlur(/* 大小 */10);
        StencilUtil.uninitStencilBuffer();

        RoundedUtil.drawRound(x,y,w,h,2,nlSetting.Light ? new Color(240,245,248,230) : new Color(7,13,23,230));

        RoundedUtil.drawRound(x + 90,y + 40,w - 90,h - 40,1,nlSetting.Light ? new Color(255,255,255) :new Color(9,9,9));

        RoundedUtil.drawRound(x + 90,y,w - 90,h - 260 , 1,nlSetting.Light ?new Color(255,255,255)  :new Color(13,13,11));

        RoundedUtil.drawRound(x + 90,y + 39,w - 90,1 , 0,nlSetting.Light ? new Color(213,213,213) : new Color(26,26,26));

        RoundedUtil.drawRound(x + 89 ,y,1,h,0,nlSetting.Light ? new Color(213,213,213) : new Color(26,26,26));

        GL11.glEnable(GL11.GL_BLEND);

        mc.getTextureManager().bindTexture(new ResourceLocation("nb"));

        RoundedUtil.drawRoundTextured(x + 4 ,y + 274,20,20,10f,1);

        Fonts.Nl_18.drawString(mc.getSession().getUsername(),x + 29 ,y + 275,nlSetting.Light ? new Color(51,51,51).getRGB() : -1);

        Fonts.Nl_16.drawString(ChatFormatting.GRAY + "Till: " + ChatFormatting.RESET + new SimpleDateFormat("dd:MM").format(new Date()) + " " + new SimpleDateFormat("HH:mm").format(new Date()),x + 29 ,y + 287,neverlosecolor.getRGB());

        if (!nlSetting.Light) {
            NLOutline("FDPCLIENT", Fonts.NLBold_28, x + 7, y + 12, -1, neverlosecolor.getRGB(), 0.7f);

        }else {
            Fonts.NLBold_28.drawString("FDP", x + 8, y + 12, new Color(51,51,51).getRGB(), false);
        }

        RoundedUtil.drawRound(x ,y + 265,89,1 , 0,nlSetting.Light ? new Color(213,213,213) :new Color(26,26,26));

        for (NlTab nlTab : nlTabs){
            nlTab.x = x;
            nlTab.y = y;
            nlTab.w = w;
            nlTab.h = h;

            nlTab.draw(mouseX,mouseY);
        }

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString("x", (float) (x + w - 50 + (search || !searchanim.isDone() ? -83 * searchanim.getOutput() : 0)),y + 17,settings ? neverlosecolor.getRGB() : NeverloseGui.INSTANCE.getLight() ? new Color(95,95,95).getRGB() :-1);

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString("j",x + w - 30,y + 18,search ? neverlosecolor.getRGB() :NeverloseGui.INSTANCE.getLight() ? new Color(95,95,95).getRGB() : -1);

        searchanim.setDirection(search ? Direction.FORWARDS : Direction.BACKWARDS);

        if (search || !searchanim.isDone()){
            RenderUtil.drawRoundedRect((float) (x + w - 30 -(85 * searchanim.getOutput() )), (float) (y + 12), (float) (80 * searchanim.getOutput()),15,1,new Color(3,13,26).getRGB(),1,NeverloseGui.INSTANCE.getLight() ? new Color(95,95,95).getRGB() : new Color(28,133,192).getRGB());
        }

        if (settings){
            nlSetting.draw(mouseX,mouseY);
        }

        RoundedUtil.drawRoundOutline(x + 105,y+10,45 + 10,16 + 5,2,0.1f,NeverloseGui.INSTANCE.getLight() ? new Color(245,245,245) : new Color(13,13,11),RenderUtil.isHovering(x + 105, y + 10, 45 + 10, 16 + 5, mouseX, mouseY) ? neverlosecolor :new Color(19,19,17));

        Fonts.Nl_18.drawString( "Save",x + 128,y+18 ,NeverloseGui.INSTANCE.getLight()? new Color(18,18,19).getRGB() : -1);

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString("K",x + 110,y+19, NeverloseGui.INSTANCE.getLight()? new Color(18,18,19).getRGB() : -1);

        GL11.glPopMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void NLOutline(String str, FontRenderer fontRenderer, float x, float y, int color, int color2, float size) {
        fontRenderer.drawString(str, x + size, y, color2, false);
        fontRenderer.drawString(str, x, y - size, color2, false);
        fontRenderer.drawString(str, x, y, color, false);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        nlTabs.forEach(e -> e.click(mouseX,mouseY,mouseButton));
        if (settings){
            nlSetting.click(mouseX,mouseY,mouseButton);
        }
        if (mouseButton ==0){
            if(RenderUtil.isHovering(x + 110,y,w - 110,h - 260 ,mouseX,mouseY)) {
                this.x2 = (int) (x - mouseX);
                this.y2 = (int) (y - mouseY);
                this.dragging = true;
            }

            if (RenderUtil.isHovering(x + 105, y + 10, 45 + 10, 16 + 5, mouseX, mouseY)) {
                if (configManager.getActiveConfig() != null) {
                    configManager.saveConfig(configManager.getActiveConfig().getName());
                } else {
                    FDPClient.fileManager.saveAllConfigs();
                    configManager.refresh();
                }
            }

            if (RenderUtil.isHovering((float) (x + w - 50+ (search || !searchanim.isDone() ? -83 * searchanim.getOutput() : 0)),y + 17,Fonts.NlIcon.nlfont_24.getNlfont_24().stringWidth("x"),Fonts.NlIcon.nlfont_24.getNlfont_24().getHeight(),mouseX,mouseY)){
                settings = !settings;
                dragging = false;
                nlSetting.x = this.x + this.w + 20;
                nlSetting.y = this.y;
            }

            if (RenderUtil.isHovering(x + w - 30,y + 18,Fonts.NlIcon.nlfont_20.getNlfont_20().stringWidth("j"),Fonts.NlIcon.nlfont_20.getNlfont_20().getHeight(),mouseX,mouseY)){
                search = !search;
                dragging = false;
            }

        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        nlTabs.forEach( e -> e.released(mouseX,mouseY,state));

        if(state == 0) {
            this.dragging = false;
        }


        if (settings){
            nlSetting.released(mouseX,mouseY,state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        nlTabs.forEach( e -> e.keyTyped(typedChar,keyCode));
        super.keyTyped(typedChar, keyCode);
    }


    public static NeverloseGui getInstance() {
        return INSTANCE;
    }

    public NeverloseConfigManager getConfigManager() {
        return configManager;
    }

    public boolean getLight(){
        return nlSetting.Light;
    }

}