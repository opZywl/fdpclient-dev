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

        Fonts.INSTANCE.getNl_16().drawString(setting.getName(), mainx + 100 + getX(), mainy + modey + 57, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);

        RenderUtil.drawRoundedRect(mainx + 170 + getX(), mainy + modey + 54, 80, 14, 2, NeverloseGui.getInstance().getLight() ? new Color(255, 255, 255).getRGB() : new Color(0, 5, 19).getRGB(), 1, new Color(13, 24, 35).getRGB());

        Fonts.INSTANCE.getNl_16().drawString(setting.get(), mainx + 173 + getX(), mainy + modey + 59, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);


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

        RenderUtil.drawArrow(mainx + 240 + getX(), mainy + modey + 55 + anim, (int) 2, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : new Color(200, 200, 200).getRGB(), length);

        if (setting.getOpenList()) {
            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);

            RenderUtil.drawRoundedRect(mainx + 170 + getX(), mainy + modey + 54 + 14, 80, setting.getValues().length * 12f, 2, NeverloseGui.getInstance().getLight() ? new Color(255, 255, 255).getRGB() : new Color(0, 5, 19).getRGB(), 1, new Color(13, 24, 35).getRGB());
            for (String option : setting.getValues()) {
                int optionIndex = getIndex(option);
                setting.get();

                Fonts.INSTANCE.getNl_15().drawString(option, mainx + 173 + getX(), mainy + modey + 59 + 12 + optionIndex * 12, option.equalsIgnoreCase(setting.get()) ? NeverloseGui.neverlosecolor.getRGB() : NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);
            }

            GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && RenderUtil.isHovering(NeverloseGui.getInstance().x + 170 + getX(), NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 54, 80, 14, mouseX, mouseY)) {
            setting.setOpenList(!setting.getOpenList());
        }

        if (mouseButton == 0) {
            if (this.setting.getOpenList()
                    && mouseX >= NeverloseGui.getInstance().x + 170 + getX()
                    && mouseX <= NeverloseGui.getInstance().x + 170 + getX() + 80
            ) {
                for (int i = 0; i < setting.getValues().length; i++) {
                    final int v = (NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 59 + 12 + i * 12);

                    if (mouseY >= v && mouseY <= v + 12) {
                        this.setting.set(this.setting.getValues()[i], true);
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