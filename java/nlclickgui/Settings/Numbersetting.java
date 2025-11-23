package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings;

import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.IntValue;
import net.ccbluex.liquidbounce.config.Value;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Numbersetting extends Downward<Value<?>> {
    public Numbersetting(Value<?> s, NlModule moduleRender) {
        super(s, moduleRender);
    }

    public float percent = 0;

    private boolean iloveyou, isset;

    private String finalvalue;

    public Animation HoveringAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);


    @Override
    public void draw(int mouseX, int mouseY) {
        int mainx = NeverloseGui.getInstance().x;
        int mainy = NeverloseGui.getInstance().y;

        int numbery = (int) (getY() + getScrollY());

        HoveringAnimation.setDirection(iloveyou || RenderUtil.isHovering(NeverloseGui.getInstance().x + 170 + getX(), NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 58, 60, 2, mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

        double clamp = MathHelper.clamp_double(Minecraft.getDebugFPS() / 30.0, 1, 9999);

        double minimum = 0;
        double maximum = 1;

        if (setting instanceof IntValue) {
            minimum = ((IntValue) setting).getMinimum();
            maximum = ((IntValue) setting).getMaximum();
        } else if (setting instanceof FloatValue) {
            minimum = ((FloatValue) setting).getMinimum();
            maximum = ((FloatValue) setting).getMaximum();
        }

        double current = ((Number) setting.get()).doubleValue();
        final double percentBar = (current - minimum) / (maximum - minimum);

        percent = Math.max(0, Math.min(1, (float) (percent + (Math.max(0, Math.min(percentBar, 1)) - percent) * (0.2 / clamp))));

        Fonts.INSTANCE.getNl_16().drawString(setting.getName(), mainx + 100 + getX(), mainy + numbery + 57, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);

        RoundedUtil.drawRound(mainx + 170 + getX(), mainy + numbery + 58, 60, 2, 2, NeverloseGui.getInstance().getLight() ? new Color(230, 230, 230) : new Color(5, 22, 41));

        RoundedUtil.drawRound(mainx + 170 + getX(), mainy + numbery + 58, 60 * percent, 2, 2, new Color(12, 100, 138));

        RoundedUtil.drawCircle(mainx + 167 + getX() + (60 * percent), mainy + numbery + 56, (float) (5.5f + (0.5f * HoveringAnimation.getOutput())), NeverloseGui.neverlosecolor);

        if (iloveyou) {
            float percentt = Math.min(1, Math.max(0, ((mouseX - (mainx + 170 + getX())) / 99.0f) * 1.55f));
            double newValue = ((percentt * (maximum - minimum)) + minimum);

            if (setting instanceof IntValue) {
                ((IntValue) setting).set((int) Math.round(newValue), true);
            } else if (setting instanceof FloatValue) {
                ((FloatValue) setting).set((float) newValue, true);
            }
        }

        if (isset) {
            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);
        }

        int stringWidth = Fonts.INSTANCE.getNl_15().stringWidth(isset ? finalvalue + "_" : current + "") + 4;

        RenderUtil.drawRoundedRect(mainx + 235 + getX(), mainy + numbery + 55, stringWidth, 9, 1, NeverloseGui.getInstance().getLight() ? new Color(255, 255, 255).getRGB() : new Color(0, 5, 19).getRGB(), 1, new Color(13, 24, 35).getRGB());

        Fonts.INSTANCE.getNl_15().drawString(isset ? finalvalue + "_" : current + "", mainx + 237 + getX(), mainy + numbery + 58, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);

        if (isset) {
            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        double current = ((Number) setting.get()).doubleValue();

        if (RenderUtil.isHovering(NeverloseGui.getInstance().x + 170 + getX(), NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 58, 60, 2, mouseX, mouseY) && !isset) {
            if (mouseButton == 0) {
                iloveyou = true;
            }
        }

        int stringWidth = Fonts.INSTANCE.getNl_15().stringWidth(isset ? finalvalue + "_" : current + "") + 4;

        if (RenderUtil.isHovering(NeverloseGui.getInstance().x + 235 + getX(), NeverloseGui.getInstance().y + (getY() + getScrollY()) + 55, stringWidth, 9, mouseX, mouseY)) {
            if (mouseButton == 0) {
                finalvalue = String.valueOf(current);
                isset = true;
            }
        } else {
            if (mouseButton == 0) {
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
        if (isset) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isset = false;
            } else if (keynumbers(keyCode)) {
                if (!(keyCode == Keyboard.KEY_PERIOD && finalvalue.contains("."))) {
                    finalvalue = (finalvalue + typedChar);
                }
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && finalvalue.length() >= 1) {
                finalvalue = (finalvalue.substring(0, finalvalue.length() - 1));
            }

            if (keyCode == Keyboard.KEY_RETURN) {
                try {
                    if (setting instanceof FloatValue) {
                        FloatValue floatSetting = (FloatValue) setting;
                        float val = Float.parseFloat(finalvalue);
                        float max = floatSetting.getMaximum();
                        float min = floatSetting.getMinimum();
                        floatSetting.set(Math.min(Math.max(val, min), max), true);
                    } else if (setting instanceof IntValue) {
                        IntValue intSetting = (IntValue) setting;
                        int val = Integer.parseInt(finalvalue);
                        int max = intSetting.getMaximum();
                        int min = intSetting.getMinimum();
                        intSetting.set(Math.min(Math.max(val, min), max), true);
                    }
                } catch (NumberFormatException e) {
                }

                isset = false;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    public boolean keynumbers(int keyCode) {
        return (keyCode == Keyboard.KEY_0 || keyCode == Keyboard.KEY_1 || keyCode == Keyboard.KEY_2 || keyCode == Keyboard.KEY_3 || keyCode == Keyboard.KEY_4 || keyCode == Keyboard.KEY_6 || keyCode == Keyboard.KEY_5 || keyCode == Keyboard.KEY_7 || keyCode == Keyboard.KEY_8 || keyCode == Keyboard.KEY_9 || keyCode == Keyboard.KEY_PERIOD || keyCode == Keyboard.KEY_MINUS);
    }
}