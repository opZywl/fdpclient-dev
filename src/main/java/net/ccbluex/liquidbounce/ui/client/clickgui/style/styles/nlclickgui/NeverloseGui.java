package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.SideGui.SideGui;
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
import net.ccbluex.liquidbounce.ui.client.gui.GuiSpotify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NeverloseGui extends GuiScreen {

    public static NeverloseGui INSTANCE;

    public int x,y,w,h;

    public Animation alphaani;

    public static Color neverlosecolor = new Color(28,133,192);

    public NlSub selectedSub = null;

    public List<NlTab> nlTabs = new ArrayList<>();

    public boolean Loader = true;

    private final SideGui sideGui = new SideGui();

    private int x2;
    private int y2;

    private boolean dragging,settings,search;
    private String searchText = "";

    private final ResourceLocation defaultAvatar = new ResourceLocation(FDPClient.CLIENT_NAME.toLowerCase() + "/64.png");
    private final ResourceLocation spotifyIcon = new ResourceLocation(FDPClient.CLIENT_NAME.toLowerCase() + "/spotify.png");
    private ResourceLocation avatarTexture = defaultAvatar;
    private boolean avatarLoaded;

    private NlSetting nlSetting;

    private int spotifyX;
    private int spotifyY;
    private final int spotifySize = 16;

    private Animation searchanim = new EaseInOutQuad(400,1, Direction.BACKWARDS);


    public Configs configs = new Configs();
    private final NeverloseConfigManager configManager = new NeverloseConfigManager();

    public NeverloseGui(){
        INSTANCE = this;
        x = 100;
        y = 100;
        w = 500;
        h = 380;

        int y2 = 0;
        int u2 =0;
        List<Category> orderedCategories = new ArrayList<>();
        orderedCategories.add(Category.CLIENT);

        for (Category type : Category.values()) {
            if (!orderedCategories.contains(type)) {
                orderedCategories.add(type);
            }
        }

        for (Category type : orderedCategories){
            if(type.name().equalsIgnoreCase("World") ||
                    type.name().equalsIgnoreCase("Interface")) continue;

            nlTabs.add(new NlTab(type,u2 + y2 + 40));

            for (Category.SubCategory subCategory: type.getSubCategories()){
                u2 += 17;
            }

            y2 += 14;
        }

        nlSetting = new NlSetting();

    }


    @Override
    public void initGui() {
        super.initGui();
        configManager.refresh();

        alphaani = new EaseInOutQuad(300,0.6,Direction.FORWARDS);
        sideGui.initGui();
    }

    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GL11.glPushMatrix();





        if (Loader && !nlTabs.isEmpty()){
            selectedSub = nlTabs.get(0).nlSubList.get(0);
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

        RoundedUtil.drawRound(x + 90,getContentTop(),w - 90,getContentHeight(),1,nlSetting.Light ? new Color(255,255,255) :new Color(0,0,0,235));

        RoundedUtil.drawRound(x + 90,y,w - 90,getContentTopOffset() , 1,nlSetting.Light ?new Color(255,255,255)  :new Color(13,13,11));

        RoundedUtil.drawRound(x + 90,y + 39,w - 90,1 , 0,nlSetting.Light ? new Color(213,213,213) : new Color(26,26,26));

        RoundedUtil.drawRound(x + 89 ,y,1,h,0,nlSetting.Light ? new Color(213,213,213) : new Color(26,26,26));

        GL11.glEnable(GL11.GL_BLEND);

        ensureAvatarTexture();
        mc.getTextureManager().bindTexture(avatarTexture);

        int footerLineY = y + h - 35;
        int avatarY = footerLineY + 9;

        RoundedUtil.drawRoundTextured(x + 4 , avatarY,20,20,10f,1);

        Fonts.Nl_18.drawString(mc.getSession().getUsername(),x + 29 , avatarY + 1,nlSetting.Light ? new Color(51,51,51).getRGB() : -1);

        Fonts.Nl_16.drawString(ChatFormatting.GRAY + "Till: " + ChatFormatting.RESET + new SimpleDateFormat("dd:MM").format(new Date()) + " " + new SimpleDateFormat("HH:mm").format(new Date()),x + 29 , avatarY + 13,neverlosecolor.getRGB());

        if (!nlSetting.Light) {
            NLOutline("FDPCLIENT", Fonts.NLBold_28, x + 7, y + 12, -1, neverlosecolor.getRGB(), 0.7f);

        }else {
            Fonts.NLBold_28.drawString("FDP", x + 8, y + 12, new Color(51,51,51).getRGB(), false);
        }

        RoundedUtil.drawRound(x , footerLineY,89,1 , 0,nlSetting.Light ? new Color(213,213,213) :new Color(26,26,26));

        spotifyX = x + 165;
        spotifyY = y + 14;
        RenderUtil.drawImage(spotifyIcon, spotifyX, spotifyY, spotifySize, spotifySize);

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
            Fonts.Nl_16.drawString(searchText, (float) (x + w - 26 -(85 * searchanim.getOutput() )), y + 15, NeverloseGui.INSTANCE.getLight() ? new Color(95,95,95).getRGB() : -1);
        }

        if (settings){
            nlSetting.draw(mouseX,mouseY);
        }

        RoundedUtil.drawRoundOutline(x + 105,y+10,45 + 10,16 + 5,2,0.1f,NeverloseGui.INSTANCE.getLight() ? new Color(245,245,245) : new Color(13,13,11),RenderUtil.isHovering(x + 105, y + 10, 45 + 10, 16 + 5, mouseX, mouseY) ? neverlosecolor :new Color(19,19,17));

        Fonts.Nl_18.drawString( "Save",x + 128,y+18 ,NeverloseGui.INSTANCE.getLight()? new Color(18,18,19).getRGB() : -1);

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString("K",x + 110,y+19, NeverloseGui.INSTANCE.getLight()? new Color(18,18,19).getRGB() : -1);

        GL11.glPopMatrix();
        sideGui.drawScreen(mouseX, mouseY, partialTicks, 255);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void ensureAvatarTexture() {
        if (!avatarLoaded) {
            avatarTexture = defaultAvatar;
            avatarLoaded = true;
        }
    }

    public static void NLOutline(String str, FontRenderer fontRenderer, float x, float y, int color, int color2, float size) {
        fontRenderer.drawString(str, x + size, y, color2, false);
        fontRenderer.drawString(str, x, y - size, color2, false);
        fontRenderer.drawString(str, x, y, color, false);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boolean wasFocused = sideGui.getFocused();
        sideGui.mouseClicked(mouseX, mouseY, mouseButton);

        if (!wasFocused) {
            nlTabs.forEach(e -> e.click(mouseX,mouseY,mouseButton));
            if (settings){
                nlSetting.click(mouseX,mouseY,mouseButton);
            }
            if (mouseButton ==0){
                if (RenderUtil.isHovering(spotifyX, spotifyY, spotifySize, spotifySize, mouseX, mouseY)) {
                    mc.displayGuiScreen(new GuiSpotify(this));
                    return;
                }

                if(RenderUtil.isHovering(x + 110,y,w - 110,h - 300 ,mouseX,mouseY)) {
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
                    if (!search) {
                        searchText = "";
                    }
                }

            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        boolean wasFocused = sideGui.getFocused();
        sideGui.mouseReleased(mouseX, mouseY, state);

        if (!wasFocused) {
            nlTabs.forEach( e -> e.released(mouseX,mouseY,state));

            if(state == 0) {
                this.dragging = false;
            }


            if (settings){
                nlSetting.released(mouseX,mouseY,state);
            }
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        sideGui.keyTyped(typedChar, keyCode);

        if (search) {
            if (keyCode == 1) {
                search = false;
                searchText = "";
                return;
            }
            if (keyCode == 14) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return;
            }
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchText = searchText + typedChar;
                return;
            }
        }

        nlTabs.forEach( e -> e.keyTyped(typedChar,keyCode));
        super.keyTyped(typedChar, keyCode);
    }

    public boolean isSearching() {
        return search && !searchText.isEmpty();
    }

    public String getSearchText() {
        return searchText;
    }


    public int getContentTopOffset() {
        return Math.max(70, h - 290);
    }

    public int getContentTop() {
        return y + getContentTopOffset();
    }

    public int getContentHeight() {
        return h - getContentTopOffset() - 40;
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