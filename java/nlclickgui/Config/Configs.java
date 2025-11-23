package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.config.SettingsUtils;
import net.ccbluex.liquidbounce.handler.api.ClientApi;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Configs {

    private int posx, posy, scy;
    private float areaWidth;

    private boolean showLocalConfigs = false;
    private final List<ButtonArea> interactiveAreas = new ArrayList<>();
    private int contentHeight;

    // --- VARIÁVEIS DE OTIMIZAÇÃO (CACHE) ---
    private List<?> onlineConfigsCache = null;
    private boolean isLoadingOnline = false;
    // ----------------------------------------

    public void setBounds(int posx, int posy, float areaWidth) {
        this.posx = posx;
        this.posy = posy;
        this.areaWidth = areaWidth;
    }

    public void setScroll(int scy) {
        this.scy = scy;
    }

    public void draw(int mx, int my) {
        interactiveAreas.clear();
        int baseX = posx + 10;
        int baseY = posy + scy + 10;

        int alpha = 255;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int buttonToggleWidth = 70;

        int openFolderWidth = buttonToggleWidth * 2;

        // Botão OPEN FOLDER
        drawButton(baseX, baseY, openFolderWidth, buttonHeight, mx, my, NeverloseGui.getInstance().getLight(), false);
        // Texto adapta a cor dependendo do fundo do botão
        Fonts.InterBold_26.drawString("OPEN FOLDER", baseX + 10, baseY + 5, applyTextColor(alpha, false));
        interactiveAreas.add(new ButtonArea(baseX, baseY, openFolderWidth, buttonHeight, this::openFolder));

        int togglesY = baseY + buttonHeight + buttonSpacing;

        // Botão ONLINE
        boolean onlineActive = !showLocalConfigs;
        drawToggle(baseX, togglesY, buttonToggleWidth, buttonHeight, mx, my, onlineActive);
        Fonts.InterBold_26.drawString("ONLINE", baseX + 10, togglesY + 5, applyTextColor(alpha, onlineActive));
        interactiveAreas.add(new ButtonArea(baseX, togglesY, buttonToggleWidth, buttonHeight, () -> {
            showLocalConfigs = false;
            if (onlineConfigsCache == null) {
                loadOnlineConfigsAsync();
            }
        }));

        // Botão LOCAL
        // AQUI ESTAVA O ERRO: Recriando a variável localX
        int localX = baseX + buttonToggleWidth + buttonSpacing;

        boolean localActive = showLocalConfigs;
        drawToggle(localX, togglesY, buttonToggleWidth, buttonHeight, mx, my, localActive);
        Fonts.InterBold_26.drawString("LOCAL", localX + 10, togglesY + 5, applyTextColor(alpha, localActive));
        interactiveAreas.add(new ButtonArea(localX, togglesY, buttonToggleWidth, buttonHeight, () -> showLocalConfigs = true));

        int listStartY = togglesY + buttonHeight + buttonSpacing;

        if (!showLocalConfigs && onlineConfigsCache == null && !isLoadingOnline) {
            loadOnlineConfigsAsync();
        }

        drawConfigList(mx, my, listStartY, alpha);

        contentHeight = (listStartY - (posy + scy)) + getListHeight();
    }

    private void loadOnlineConfigsAsync() {
        if (isLoadingOnline) return;
        isLoadingOnline = true;

        new Thread(() -> {
            try {
                List<?> configs = ClientApi.INSTANCE.getSettingsList("legacy");
                synchronized (this) {
                    onlineConfigsCache = configs;
                    isLoadingOnline = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isLoadingOnline = false;
            }
        }).start();
    }

    public void click(int mx, int my, int mb) {
        if (mb != 0) {
            return;
        }
        for (ButtonArea area : interactiveAreas) {
            if (RenderUtil.isHovering(area.x, area.y, area.width, area.height, mx, my)) {
                area.action.run();
                break;
            }
        }
    }

    public int getContentHeight() {
        return contentHeight;
    }

    private void drawToggle(int x, int y, int width, int height, int mx, int my, boolean active) {
        boolean hovered = RenderUtil.isHovering(x, y, width, height, mx, my);
        // Correção de cores para Light Mode
        Color base = NeverloseGui.getInstance().getLight() ? new Color(220, 222, 225) : new Color(50, 50, 50);
        Color activeColor = new Color(100, 150, 100);
        Color hoverColor = NeverloseGui.getInstance().getLight() ? new Color(200, 200, 205) : new Color(70, 70, 70);
        Color fill = active ? activeColor : hovered ? hoverColor : base;
        drawButton(x, y, width, height, fill);
    }

    private void drawButton(int x, int y, int width, int height, int mx, int my, boolean light, boolean active) {
        boolean hovered = RenderUtil.isHovering(x, y, width, height, mx, my);
        // Correção de cores para Light Mode
        Color base = light ? new Color(220, 222, 225) : new Color(50, 50, 50);
        Color hover = light ? new Color(200, 200, 205) : new Color(70, 70, 70);
        Color fill = active ? new Color(100, 150, 100) : hovered ? hover : base;
        drawButton(x, y, width, height, fill);
    }

    private void drawButton(int x, int y, int width, int height, Color fill) {
        RoundedUtil.drawRound(x, y, width, height, 3, fill);
    }

    private void drawConfigList(int mx, int my, int startY, int alpha) {
        float buttonWidth = (areaWidth - 50) / 4f - 10f;
        int buttonHeight = 20;
        int configsPerRow = 4;
        float configX = posx + 10;
        float configY = startY;
        int configCount = 0;

        // Texto padrão (não é botão ativo)
        int standardTextColor = applyTextColor(alpha, false);

        if (showLocalConfigs) {
            File[] localConfigs = FDPClient.fileManager.getSettingsDir().listFiles((dir, name) -> name.endsWith(".txt"));
            if (localConfigs != null && localConfigs.length > 0) {
                for (File file : localConfigs) {
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY, () -> loadLocalConfig(file));
                    Fonts.InterBold_26.drawString(file.getName().replace(".txt", ""), configX + 5, configY + 5, standardTextColor);
                    configX += buttonWidth + 10;
                    configCount++;
                    if (configCount % configsPerRow == 0) {
                        configX = posx + 10;
                        configY += buttonHeight + 5;
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No local configurations available.", configX, configY, standardTextColor);
            }
        } else {
            if (isLoadingOnline) {
                Fonts.InterBold_26.drawString("Loading online configs...", configX, configY, standardTextColor);
                return;
            }

            List<?> remoteSettings = onlineConfigsCache;

            if (remoteSettings != null && !remoteSettings.isEmpty()) {
                List<?> safeList;
                synchronized(this) {
                    safeList = new ArrayList<>(remoteSettings);
                }

                for (Object autoSetting : safeList) {
                    String settingName = getSettingName(autoSetting);
                    String settingId = getSettingId(autoSetting);
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY, () -> loadOnlineConfig(settingId, settingName));
                    Fonts.InterBold_26.drawString(settingName, configX + 5, configY + 5, standardTextColor);
                    configX += buttonWidth + 10;
                    configCount++;
                    if (configCount % configsPerRow == 0) {
                        configX = posx + 10;
                        configY += buttonHeight + 5;
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No online configurations or failed to load.", configX, configY, standardTextColor);
            }
        }
    }

    private void drawConfigButton(int mx, int my, float width, float height, float configX, float configY, Runnable action) {
        boolean hovered = RenderUtil.isHovering(configX, configY, width, height, mx, my);
        // Correção de cores para Light Mode
        Color base = NeverloseGui.getInstance().getLight() ? new Color(220, 222, 225) : new Color(50, 50, 50);
        Color hover = NeverloseGui.getInstance().getLight() ? new Color(200, 200, 205) : new Color(70, 70, 70);
        Color fill = hovered ? hover : base;
        RoundedUtil.drawRound(configX, configY, width, height, 3, fill);
        interactiveAreas.add(new ButtonArea(configX, configY, width, height, action));
    }

    private int getListHeight() {
        int itemCount = 0;
        int rowHeight = 25;
        if (showLocalConfigs) {
            File[] localConfigs = FDPClient.fileManager.getSettingsDir().listFiles((dir, name) -> name.endsWith(".txt"));
            itemCount = localConfigs == null ? 0 : localConfigs.length;
        } else {
            if (onlineConfigsCache != null) {
                itemCount = onlineConfigsCache.size();
            } else {
                itemCount = 0;
            }
        }
        if (itemCount == 0) {
            return rowHeight + 5;
        }
        int rows = (int) Math.ceil(itemCount / 4.0);
        return rows * rowHeight;
    }

    private void loadLocalConfig(File file) {
        String configName = file.getName().replace(".txt", "");
        try {
            ClientUtils.INSTANCE.displayChatMessage("Loading local configuration: " + configName + "...");
            String localConfigContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            SettingsUtils.INSTANCE.applyScript(localConfigContent);
            ClientUtils.INSTANCE.displayChatMessage("Local configuration " + configName + " loaded successfully!");
        } catch (IOException e) {
            ClientUtils.INSTANCE.displayChatMessage("Error loading local configuration: " + e.getMessage());
        }
    }

    private void loadOnlineConfig(String settingId, String configName) {
        new Thread(() -> {
            try {
                ClientUtils.INSTANCE.displayChatMessage("Downloading configuration: " + configName + "...");
                String configScript = ClientApi.INSTANCE.getSettingsScript("legacy", settingId);
                SettingsUtils.INSTANCE.applyScript(configScript);
                ClientUtils.INSTANCE.displayChatMessage("Configuration " + configName + " loaded successfully!");
            } catch (Exception e) {
                ClientUtils.INSTANCE.displayChatMessage("Error loading configuration: " + e.getMessage());
            }
        }).start();
    }

    private String getSettingName(Object autoSetting) {
        try {
            Method method = autoSetting.getClass().getMethod("getName");
            Object value = method.invoke(autoSetting);
            return value == null ? "" : value.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getSettingId(Object autoSetting) {
        try {
            Method method = autoSetting.getClass().getMethod("getSettingId");
            Object value = method.invoke(autoSetting);
            return value == null ? "" : value.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private void openFolder() {
        try {
            Desktop.getDesktop().open(FDPClient.fileManager.getSettingsDir());
            ClientUtils.INSTANCE.displayChatMessage("Opening configuration folder...");
        } catch (IOException e) {
            ClientUtils.INSTANCE.displayChatMessage("Error opening folder: " + e.getMessage());
        }
    }

    private int applyTextColor(int alpha, boolean isActive) {
        if (isActive) {
            return new Color(255, 255, 255, alpha).getRGB();
        }
        if (NeverloseGui.getInstance().getLight()) {
            return new Color(30, 30, 30, alpha).getRGB(); // Preto para fundo claro
        }
        return new Color(255, 255, 255, alpha).getRGB(); // Branco para fundo escuro
    }

    private int applyTextColor(int alpha) {
        return applyTextColor(alpha, false);
    }

    private static class ButtonArea {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final Runnable action;

        public ButtonArea(float x, float y, float width, float height, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }
    }
}