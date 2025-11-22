package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings;

import net.ccbluex.liquidbounce.config.ColorValue;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;

import java.awt.*;

public class ColorSetting extends Downward<ColorValue> {

    public ColorSetting(ColorValue s, NlModule moduleRender) {
        super(s, moduleRender);
    }

    @Override
    public void draw(int mouseX, int mouseY) {
        int mainx = NeverloseGui.getInstance().x;
        int mainy = NeverloseGui.getInstance().y;

        int baseOffset = NeverloseGui.getInstance().getContentTopOffset();

        int colory = (int) (getY() + getScrollY());

        Fonts.INSTANCE.getNl_16().drawString(setting.getName(), mainx + 100 + getX(), mainy + baseOffset + colory + 7, NeverloseGui.getInstance().getLight() ? new Color(95, 95, 95).getRGB() : -1);

        Color color = setting.selectedColor();
        RoundedUtil.drawRound(mainx + 100 + getX() + 138, mainy + baseOffset + colory + 2, 16, 10, 2, color);
        RenderUtil.drawBorderedRect(mainx + 100 + getX() + 138, mainy + baseOffset + colory + 2, mainx + 100 + getX() + 154, mainy + baseOffset + colory + 12, 1, new Color(0, 0, 0, 60).getRGB(), new Color(0, 0, 0, 80).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 1 && RenderUtil.isHovering(NeverloseGui.getInstance().x + 100 + getX() + 138, NeverloseGui.getInstance().y + NeverloseGui.getInstance().getContentTopOffset() + (int) (getY() + getScrollY()) + 2, 16, 10, mouseX, mouseY)) {
            setting.setRainbow(!setting.getRainbow());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }
}