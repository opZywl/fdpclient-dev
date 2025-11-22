package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class Configs {

    public int posx, posy, scy;

    public boolean[] loads = new boolean[]{true, true, true, true, true, true};

    public Configs() {

    }

    public void draw(int mx, int my) {
        posy = posy + scy;

        int i = 0;
        NeverloseConfigManager manager = NeverloseGui.getInstance().getConfigManager();
        List<NeverloseConfig> configs = manager.getConfigs();
        NeverloseConfig activeConfig = manager.getActiveConfig();

        for (NeverloseConfig c : configs) {

            RoundedUtil.drawRoundOutline(posx + 100, (posy + 50) + (42 * i) + 5, 300, 38 - 3, 3, 0.1f, NeverloseGui.getInstance().getLight() ? new Color(245, 245, 245) : new Color(15, 15, 19), NeverloseGui.getInstance().getLight() ? new Color(213, 213, 213) : new Color(23, 23, 25));

            // CORREÇÃO: Fonts.INSTANCE.getNl_20() (pois Nl_24 não existe no seu Fonts.kt)
            Fonts.INSTANCE.getNl_20().drawString(c.getName(), posx + 100 + 5, (posy + 50) + (42 * i) + 10, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : -1);

            SimpleDateFormat sdf2 = new SimpleDateFormat("MM.dd HH:mm");

            // CORREÇÃO: Fonts.INSTANCE.getNl_18()
            Fonts.INSTANCE.getNl_18().drawString(ChatFormatting.GRAY + "Modified: " + ChatFormatting.RESET + sdf2.format(c.getFile().lastModified()), posx + 100 + 5, (posy + 50) + (42 * i) + 28, new Color(10, 122, 182).getRGB());

            // CORREÇÃO: Fonts.INSTANCE.getNl_18()
            Fonts.INSTANCE.getNl_18().drawString(ChatFormatting.GRAY + "Author: " + ChatFormatting.RESET + c.getAuthor(), posx + 100 + 5 + Fonts.INSTANCE.getNl_18().stringWidth(ChatFormatting.GRAY + "Modified: " + ChatFormatting.RESET + sdf2.format(c.getFile().lastModified())) + 5, (posy + 50) + (42 * i) + 28, new Color(10, 122, 182).getRGB());

            RoundedUtil.drawRoundOutline(posx + 100 + 250 - 2, (posy + 50) + (42 * i) + 14, 45, 16, 3, 0.1f, (activeConfig == c) ? NeverloseGui.getInstance().getLight() ? new Color(255, 255, 255) : new Color(12, 17, 20) : new Color(10, 122, 182), new Color(10, 122, 182));

            // CORREÇÃO: Fonts.INSTANCE.getNLBold_18()
            Fonts.INSTANCE.getNLBold_18().drawString((activeConfig == c) ? "Save" : "Load", posx + 100 + 267, (posy + 50) + (42 * i) + 19, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : -1);

            // CORREÇÃO: Fonts.INSTANCE.getNlfont_20()
            Fonts.INSTANCE.getNlfont_20().drawString((activeConfig == c) ? "K" : "k", posx + 100 + 253, (posy + 50) + (42 * i) + 19, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : -1);

            // CORREÇÃO: Fonts.INSTANCE.getNlfont_28()
            Fonts.INSTANCE.getNlfont_28().drawString("u", posx + 100 + 230, (posy + 50) + (42 * i) + 18, NeverloseGui.getInstance().getLight() ? c.isExpanded() ? NeverloseGui.neverlosecolor.getRGB() : new Color(18, 18, 19).getRGB() : c.isExpanded() ? NeverloseGui.neverlosecolor.getRGB() : -1);

            if (c.isExpanded()) {
                GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) 2.0f);
                RoundedUtil.drawRoundOutline(posx + 100 + 230 + 20, (posy + 50) + (42 * i) + 18, 60, 47, 2, 0.1f, NeverloseGui.getInstance().getLight() ? new Color(245, 245, 245) : new Color(20, 19, 26), NeverloseGui.getInstance().getLight() ? new Color(213, 213, 213) : new Color(23, 23, 25));

                // Reload Button
                // CORREÇÃO: Fonts.INSTANCE.getNl_19()
                Fonts.INSTANCE.getNl_19().drawString("Reload", posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25, Fonts.INSTANCE.getNl_19().stringWidth("Reload"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my) ? -1 : new Color(159, 165, 170).getRGB());

                // Icon 'o'
                // CORREÇÃO: Fonts.INSTANCE.getNlfont_20()
                Fonts.INSTANCE.getNlfont_20().drawString("o", posx + 100 + 230 + 23, (posy + 50) + (42 * i) + 25, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : new Color(159, 165, 170).getRGB());

                // Save Button
                Fonts.INSTANCE.getNl_19().drawString("Save", posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + Fonts.INSTANCE.getNl_19().getHeight() + 7, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + Fonts.INSTANCE.getNl_19().getHeight() + 7, Fonts.INSTANCE.getNl_19().stringWidth("Save"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my) ? -1 : new Color(159, 165, 170).getRGB());

                // Icon 'K'
                Fonts.INSTANCE.getNlfont_20().drawString("K", posx + 100 + 230 + 23, (posy + 50) + (42 * i) + 25 + Fonts.INSTANCE.getNl_19().getHeight() + 7, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : new Color(159, 165, 170).getRGB());

                // Delete Button
                Fonts.INSTANCE.getNl_19().drawString("Delete", posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + +((Fonts.INSTANCE.getNl_19().getHeight() + 7) * 2), NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + +((Fonts.INSTANCE.getNl_19().getHeight() + 7) * 2), Fonts.INSTANCE.getNl_19().stringWidth("Delete"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my) ? -1 : new Color(159, 165, 170).getRGB());

                // Icon 'O'
                Fonts.INSTANCE.getNlfont_20().drawString("O", posx + 100 + 230 + 23, (posy + 50) + (42 * i) + 25 + ((Fonts.INSTANCE.getNl_19().getHeight() + 7) * 2), NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : new Color(159, 165, 170).getRGB());

                GL11.glTranslatef((float) 0.0f, (float) 0.0f, (float) -2.0f);
            }

            i++;
        }
    }

    public int getYY() {
        int i = 0;
        for (NeverloseConfig ignored : NeverloseGui.getInstance().getConfigManager().getConfigs()) {
            i++;
        }
        return (posy + 50) + (42 * i) + 5;
    }

    public void click(int mx, int my, int mb) {
        int i = 0;
        NeverloseConfigManager manager = NeverloseGui.getInstance().getConfigManager();
        NeverloseConfig activeConfig = manager.getActiveConfig();
        List<NeverloseConfig> configs = manager.getConfigs();

        for (NeverloseConfig c : configs) {

            // CORREÇÃO: Fonts.INSTANCE.getNlfont_28()
            if (RenderUtil.isHovering(posx + 100 + 230, (posy + 50) + (42 * i) + 18, Fonts.INSTANCE.getNlfont_28().stringWidth("u"), Fonts.INSTANCE.getNlfont_28().getHeight(), mx, my)) {
                manager.toggleExpansion(c);
            }

            // CORREÇÃO: Fonts.INSTANCE.getNl_19()
            if (RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25, Fonts.INSTANCE.getNl_19().stringWidth("Reload"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my)) {
                if (c.isExpanded()) {
                    manager.loadConfig(c.getName());
                    activeConfig = manager.getActiveConfig();
                }
            }

            if (RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + Fonts.INSTANCE.getNl_19().getHeight() + 7, Fonts.INSTANCE.getNl_19().stringWidth("Save"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my)) {
                if (c.isExpanded()) {
                    manager.saveConfig(c.getName());
                    activeConfig = manager.getActiveConfig();
                }
            }

            if (RenderUtil.isHovering(posx + 100 + 230 + 36, (posy + 50) + (42 * i) + 25 + +((Fonts.INSTANCE.getNl_19().getHeight() + 7) * 2), Fonts.INSTANCE.getNl_19().stringWidth("Delete"), Fonts.INSTANCE.getNl_19().getHeight(), mx, my)) {
                if (c.isExpanded()) {
                    manager.deleteConfig(c);
                    configs = manager.getConfigs();
                    activeConfig = manager.getActiveConfig();
                }
            }

            if (c.isExpanded()) return;
            if (RenderUtil.isHovering(posx + 100 + 250 - 2, (posy + 50) + (42 * i) + 14, 45, 16, mx, my)) {

                if ((activeConfig == c)) {
                    manager.saveConfig(c.getName());
                }

                if (!(activeConfig == c)) {
                    manager.loadConfig(c.getName());
                    activeConfig = c;
                }
            }
            i++;
        }
    }
}