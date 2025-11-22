package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings;

import net.ccbluex.liquidbounce.config.ListValue;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static net.minecraft.client.Minecraft.getDebugFPS;

public class StringsSetting extends Downward<ListValue> {

    public StringsSetting(ListValue s, NlModule moduleRender) {
        super(s, moduleRender);
    }


    private double length = 3, anim = 5;

    @Override
    public void draw(int mouseX, int mouseY) {
        int mainx = NeverloseGui.getInstance().x;
        int mainy = NeverloseGui.getInstance().y;

        int modey = (int) (getY() + getScrollY());

        Fonts.Nl_16.drawString(setting.getName(),mainx + 100 + getX(),mainy + modey + 57,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1 );

        RenderUtil.drawRoundedRect(mainx + 170 + getX(),mainy + modey + 54, 80,14,2,NeverloseGui.getInstance().getLight() ? new Color(255,255,255).getRGB() : new Color(0,5,19).getRGB(),1,new Color(13,24,35).getRGB());

        Fonts.Nl_16.drawString(setting.get(),mainx + 173 + getX(),mainy + modey + 59,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1);


        double val = getDebugFPS() / 8.3;
        if (setting.getOpenList() && length > -3) {
            length -= 3 / val;
        } else if (!setting.getOpenList() && length < 3) {
            length += 3 / val;
        }
        if (setting.getOpenList() && anim < 8) {
            anim += 3 / val;
        } else if (!setting.getOpenList() && anim > 5) {
            anim -= 3 / val;
        }

        RenderUtil.drawArrow(mainx + 240+ getX(),mainy + modey + 55 + anim, (int) 2,  NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() : new Color(200,200,200).getRGB(), length);

        if (setting.getOpenList()) {
            //循环添加Strings

            //覆盖下面的Value
            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);

           // RoundedUtil.drawRound(mainx + 91 + getX(), mainy + 35 +12 + y, 70, setting.getModes().length * 12f, 2, new Color(45, 46, 53));
            RenderUtil.drawRoundedRect(mainx + 170 + getX(),mainy + modey + 54 + 14, 80, setting.getValues().length * 12f,2, NeverloseGui.getInstance().getLight() ? new Color(255,255,255).getRGB() :new Color(0,5,19).getRGB(),1,new Color(13,24,35).getRGB());
            for (String option : setting.getValues()) {
                int optionIndex = getIndex(option);
                if (option.equalsIgnoreCase(setting.get())){
                  //  RoundedUtil.drawRound(mainx + 91 + 69 + getX(),mainy + 38 + 11 +  y + setting.getModeListinde(option)* 12, 1,8,1,new Color(ClickGui.colorValue.getValue()));
                }

                Fonts.Nl_15.drawString(option,mainx + 173 + getX(),mainy + modey + 59 + 12 + optionIndex * 12 , option.equalsIgnoreCase(setting.get()) ? NeverloseGui.neverlosecolor.getRGB() : NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() : -1);
            }

            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
        }

    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && RenderUtil.isHovering(NeverloseGui.getInstance().x + 170 + getX(), NeverloseGui.getInstance().y  +  (int) (getY() + getScrollY()) + 54, 80,14,mouseX,mouseY)){
            setting.setOpenList(!setting.getOpenList());
        }

        if (mouseButton == 0) {
            if (this.setting.getOpenList() //在这个x里面
                    && mouseX >=NeverloseGui.getInstance().x + 170 + getX() // 最小x
                    && mouseX <= NeverloseGui.getInstance().x + 170 + getX() + 80 // 最大x
            ) {
                //循环判断点击
                for (int i = 0; i < setting.getValues().length; i++) {
                    //判断Y
                    final int v = (NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 59 + 12  + i * 12);

                    if (mouseY >= v && mouseY <= v + 12) {
                        this.setting.set(this.setting.getValues()[i]);
                        // this.setting.openList = false;
                        //   moduleRender.sub.updatepostion();
                    }

                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    private int getIndex(String option) {
        String[] values = setting.getValues();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(option)) {
                return i;
            }
        }
        return 0;
    }
}
