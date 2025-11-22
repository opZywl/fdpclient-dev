package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings;

import cn.distance.ui.cfont.impl.Fonts;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import cn.distance.values.Numbers;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static cn.distance.ui.clickguis.nlclickgui.MathUtil.incValue;

public class Numbersetting extends Downward<Numbers> {
    public Numbersetting(Numbers s, NlModule moduleRender) {
        super(s, moduleRender);
    }

    public float percent = 0;

    private boolean iloveyou,isset;

    private String finalvalue;


    public Animation HoveringAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);


    @Override
    public void draw(int mouseX, int mouseY) {
        int mainx = NeverloseGui.getInstance().x;
        int mainy = NeverloseGui.getInstance().y;

        int numbery = (int) (getY() + getScrollY());

        HoveringAnimation.setDirection(iloveyou || RenderUtil.isHovering( NeverloseGui.getInstance().x + 170 + getX(),NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 58,60,2,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

        double clamp = MathHelper.clamp_double(Minecraft.getDebugFPS() / 30, 1, 9999);
        final double percentBar = (setting.getValue().floatValue()- setting.getMinimum().floatValue()
        ) / (setting.getMaximum().floatValue() - setting.getMinimum().floatValue());

        percent = Math.max(0, Math.min(1, (float) (percent + (Math.max(0, Math.min(percentBar, 1)) - percent)* (0.2 / clamp))));

        Fonts.Nl.Nl_16.Nl_16.drawString(setting.getName(),mainx + 100 + getX(),mainy + numbery + 57,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1 );

        RoundedUtil.drawRound(mainx + 170 + getX(),mainy + numbery + 58,60,2,2,NeverloseGui.getInstance().getLight() ? new Color(230,230,230) : new Color(5,22,41));

        RoundedUtil.drawRound(mainx + 170 + getX(),mainy + numbery + 58,60 * percent,2,2, new Color(12,100,138));

        RoundedUtil.drawCircle(mainx + 167 + getX() + (60 * percent),mainy + numbery + 56, (float) (5.5f + (0.5f* HoveringAnimation.getOutput())), NeverloseGui.neverlosecolor);

        //设置新的值
        if (iloveyou){

            if (setting.value instanceof Float) {
                float percentt = Math.min(1, Math.max(0, ((mouseX - (mainx + 170 + getX())) / 99)* 1.55f));
                float newValue = (percentt * (setting.getMaximum().floatValue()
                        - setting.getMinimum().floatValue())) + setting.getMinimum().floatValue();
                float set = incValue(newValue, setting.getIncrement().floatValue());

                setting.value = set;
            } else if (setting.value instanceof Double) {
                float percentt = Math.min(1, Math.max(0, ((mouseX - (mainx + 170 + getX())) / 99)* 1.55f));
                double newValue =  ((percentt * (setting.getMaximum().doubleValue()
                                        - setting.getMinimum().intValue())) + setting.getMinimum().doubleValue());
                double set = incValue(newValue, setting.getIncrement().doubleValue());

                setting.value = set;
            } else if (setting.value instanceof Integer) {
                float percentt = Math.min(1, Math.max(0, ((mouseX - (mainx + 170 + getX())) / 99)* 1.55f));
                double newValue =  ((percentt * (setting.getMaximum().floatValue()
                        - setting.getMinimum().floatValue())) + setting.getMinimum().floatValue());

                int set = (int) incValue(newValue, setting.getIncrement().intValue());

                setting.value = set;
            }
        }

        if (isset) {
            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);
        }
        RenderUtil.drawRoundedRect(mainx + 235 + getX(),mainy + numbery + 55,Fonts.Nl.Nl_14.Nl_14.stringWidth(isset ? finalvalue + "_" : setting.getValue().floatValue() + "") + 4,9,1,NeverloseGui.getInstance().getLight() ? new Color(255,255,255).getRGB() : new Color(0,5,19).getRGB(),1,new Color(13,24,35).getRGB());

        Fonts.Nl.Nl_14.Nl_14.drawString(isset ? finalvalue + "_" : setting.getValue().floatValue() + "",mainx + 237 + getX(),mainy + numbery + 58,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1);

        if (isset) {
        GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
        }

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering( NeverloseGui.getInstance().x + 170 + getX(),NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 58,60,2,mouseX,mouseY) && !isset){
            if (mouseButton == 0) {
                iloveyou = true;
            }
        }
        if (RenderUtil.isHovering(NeverloseGui.getInstance().x + 235 + getX(),NeverloseGui.getInstance().y + (getY() + getScrollY()) + 55,Fonts.Nl.Nl_14.Nl_14.stringWidth(isset ? finalvalue + "_" : setting.getValue().floatValue() + "") + 4,9,mouseX,mouseY)){
            if (mouseButton == 0 ){
                finalvalue = String.valueOf(setting.getValue().floatValue());
                isset = true;
            }
        }else {
            if (mouseButton ==0 ){
                isset = false;
            }
        }
        
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) iloveyou = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        //输入
        if (isset) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isset = (false);
            } else if (keynumbers(keyCode)) {
                //确保只有一个小数点
                if (!(keyCode == Keyboard.KEY_PERIOD && finalvalue.contains("."))) {
                    finalvalue = (finalvalue + typedChar);
                }
            }

            //删除
            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && finalvalue.length() >= 1) {
                finalvalue = (finalvalue.substring(0, finalvalue.length() - 1));
            }

            if (keyCode == Keyboard.KEY_RETURN){
                if(setting.value instanceof Float) {
                    setting.value = (Float.parseFloat(finalvalue) > setting.getMaximum().floatValue() ? setting.getMaximum().floatValue() : Math.max(Float.parseFloat(finalvalue), setting.getMinimum().floatValue()));
                } else if (setting.value instanceof Integer) {
                    setting.value = (Integer.parseInt(finalvalue) > setting.getMaximum().intValue() ? setting.getMaximum().intValue() : Math.max(Integer.parseInt(finalvalue), setting.getMinimum().intValue()));
                } else if (setting.value instanceof Double) {
                    setting.value = (Double.parseDouble(finalvalue) > setting.getMaximum().doubleValue() ? setting.getMaximum().doubleValue() : Math.max(Double.parseDouble(finalvalue), setting.getMinimum().doubleValue()));
                }
                //设置 输入的
                isset = (false);
            }
        }

        super.keyTyped(typedChar, keyCode);
    }


    //判断输入数字
    public boolean keynumbers(int keyCode){
        return (keyCode == Keyboard.KEY_0 || keyCode == Keyboard.KEY_1 || keyCode == Keyboard.KEY_2 || keyCode == Keyboard.KEY_3 || keyCode == Keyboard.KEY_4 || keyCode == Keyboard.KEY_6 || keyCode == Keyboard.KEY_5 || keyCode == Keyboard.KEY_7 || keyCode == Keyboard.KEY_8 || keyCode == Keyboard.KEY_9 || keyCode == Keyboard.KEY_PERIOD || keyCode == Keyboard.KEY_MINUS);
    }

}
