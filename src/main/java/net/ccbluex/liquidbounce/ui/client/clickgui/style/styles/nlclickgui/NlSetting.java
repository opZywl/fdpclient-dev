package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;

public class NlSetting{

    public int x = 50,y = 100;

    private boolean dragging;

    private int x2;
    private int y2;

    public boolean Light;

    public void draw(int mx, int my){

        //移动面板
        if(dragging) {
            this.x = x2 + mx;
            this.y = y2 + my;
        }

        RoundedUtil.drawRound(x,y,160,160,3,Light ? new Color(238,240,235,230) : new Color(7,13,23,230));

        Fonts.Nl_15.drawString("About Distance",x + 13,y + 4,Light ? new Color(95,95,95).getRGB() :-1);

        Fonts.Nl_16_ICON.drawString("x",x + 2, y + 4,neverlosecolor.getRGB());

        if (!Light) {
            NLOutline("distance.me".toUpperCase(), Fonts.NLBold_35, x, y + 30, -1, neverlosecolor.getRGB(), 160, 0.7f);
        }else {
            Fonts.NLBold_35.drawCenteredString("distance.me".toUpperCase(),x + 80,y+30 ,new Color(51,51,51).getRGB());
        }

        Fonts.Nl_18.drawString((!Light ? ChatFormatting.WHITE : ChatFormatting.BLACK) + "Version: " + ChatFormatting.RESET + "Dev",x + 10,y + 65,neverlosecolor.getRGB());

        Fonts.Nl_18.drawString((!Light ? ChatFormatting.WHITE : ChatFormatting.BLACK) + "Build Type: " + ChatFormatting.RESET + "Dev",x + 10,y + 65 + Fonts.Nl_18.getHeight() + 5,neverlosecolor.getRGB());

        Fonts.Nl_18.drawString((!Light ? ChatFormatting.WHITE : ChatFormatting.BLACK) + "Build Date: " + ChatFormatting.RESET + new SimpleDateFormat("dd:MM").format(new Date()) + " " + new SimpleDateFormat("HH:mm").format(new Date()),x + 10,y + 65 + (Fonts.Nl_18.getHeight() + 5) * 2,neverlosecolor.getRGB());

        Fonts.Nl_18.drawString((!Light ? ChatFormatting.WHITE : ChatFormatting.BLACK) + "Registered to: " + ChatFormatting.RESET + "younkoo",x + 10,y + 65 + (Fonts.Nl_18.getHeight() + 5) * 3,neverlosecolor.getRGB());

        Fonts.Nl_18.drawCenteredString("distance.reborn @ 2023", x+ (160/2) , y + 65 + (Fonts.Nl_18.getHeight() + 5) * 4 + 7, Light ? new Color(95,95,95).getRGB() :-1);

        Fonts.Nl_18.drawString("Style",x + 10,y + 145,Light ? new Color(95,95,95).getRGB() :-1);

        if (Light) {
            RoundedUtil.drawRound(x + 39, y + 143, 11.5f, 11.5f, 5.5f, neverlosecolor);
        }

        RoundedUtil.drawRound(x + 40,y + 144,9.5f,9.5f,4.5f,new Color(210,210,210));

        if (!Light) {
            RoundedUtil.drawRound(x + 39 + 20, y + 143, 11.5f, 11.5f, 5.5f, neverlosecolor);
        }

        RoundedUtil.drawRound(x + 40 + 20,y + 144,9.5f,9.5f,4.5f,new Color(7,13,23,230));

    }

    public static void NLOutline(String str, FontRenderer fontRenderer, float x, float y, int color, int color2, int w, float size) {
        fontRenderer.drawCenteredString(str, x + (w/2) + size, y, color2, false);
        fontRenderer.drawCenteredString(str, x+ (w/2) , y - size, color2, false);
        fontRenderer.drawCenteredString(str, x+ (w/2) , y, color, false);
    }


    public void released(int mx ,int my,int mb) {
        if(mb == 0) {
            this.dragging = false;
        }
    }

    public void click(int mx ,int my,int mb){

        if (mb ==0) {
            //移动面板
            if (RenderUtil.isHovering(x,y,160,160, mx, my)) {
                this.x2 = (int) (x - mx);
                this.y2 = (int) (y - my);
                this.dragging = true;
            }
            if (RenderUtil.isHovering(x + 40 + 20,y + 144,9.5f,9.5f,mx,my)){
                Light = false;
                this.dragging = false;
            }
            if (RenderUtil.isHovering(x + 40,y + 144,9.5f,9.5f,mx,my)){
                Light = true;
                this.dragging = false;
            }
        }
    }

}
