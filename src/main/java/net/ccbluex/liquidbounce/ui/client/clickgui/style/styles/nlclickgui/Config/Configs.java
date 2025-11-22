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

    private boolean showLocalConfigs;
    private final List<ButtonArea> interactiveAreas = new ArrayList<>();
    private int contentHeight;

    private final List<RemoteSetting> remoteSettings = new ArrayList<>();
    private boolean loadingRemoteSettings;
    private String remoteError;

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

        if (!showLocalConfigs) {
            ensureRemoteSettingsLoaded();
        }

        int alpha = 255;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int buttonToggleWidth = 70;

        int openFolderWidth = buttonToggleWidth * 2;
        drawButton(baseX, baseY, openFolderWidth, buttonHeight, mx, my, NeverloseGui.getInstance().getLight(), false);
        Fonts.InterBold_26.drawString("OPEN FOLDER", baseX + 10, baseY + 5, applyTextColor(alpha));
        interactiveAreas.add(new ButtonArea(baseX, baseY, openFolderWidth, buttonHeight, this::openFolder));

        int togglesY = baseY + buttonHeight + buttonSpacing;
        drawToggle(baseX, togglesY, buttonToggleWidth, buttonHeight, mx, my, !showLocalConfigs);
        Fonts.InterBold_26.drawString("ONLINE", baseX + 10, togglesY + 5, applyTextColor(alpha));
        interactiveAreas.add(new ButtonArea(baseX, togglesY, buttonToggleWidth, buttonHeight, () -> {
            showLocalConfigs = false;
            ensureRemoteSettingsLoaded();
        }));

        int localX = baseX + buttonToggleWidth + buttonSpacing;
        drawToggle(localX, togglesY, buttonToggleWidth, buttonHeight, mx, my, showLocalConfigs);
        Fonts.InterBold_26.drawString("LOCAL", localX + 10, togglesY + 5, applyTextColor(alpha));
        interactiveAreas.add(new ButtonArea(localX, togglesY, buttonToggleWidth, buttonHeight, () -> showLocalConfigs = true));

        int listStartY = togglesY + buttonHeight + buttonSpacing;
        drawConfigList(mx, my, listStartY, alpha);

        contentHeight = (listStartY - (posy + scy)) + getListHeight();
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
        Color base = NeverloseGui.getInstance().getLight() ? new Color(245, 245, 245) : new Color(50, 50, 50);
        Color activeColor = new Color(100, 150, 100);
        Color hoverColor = NeverloseGui.getInstance().getLight() ? new Color(225, 225, 225) : new Color(70, 70, 70);
        Color fill = active ? activeColor : hovered ? hoverColor : base;
        drawButton(x, y, width, height, fill);
    }

    private void drawButton(int x, int y, int width, int height, int mx, int my, boolean light, boolean active) {
        boolean hovered = RenderUtil.isHovering(x, y, width, height, mx, my);
        Color base = light ? new Color(245, 245, 245) : new Color(50, 50, 50);
        Color hover = light ? new Color(225, 225, 225) : new Color(70, 70, 70);
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

        if (showLocalConfigs) {
            File[] localConfigs = FDPClient.fileManager.getSettingsDir().listFiles((dir, name) -> name.endsWith(".txt"));
            if (localConfigs != null && localConfigs.length > 0) {
                for (File file : localConfigs) {
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY, () -> loadLocalConfig(file));
                    Fonts.InterBold_26.drawString(file.getName().replace(".txt", ""), configX + 5, configY + 5, applyTextColor(alpha));
                    configX += buttonWidth + 10;
                    configCount++;
                    if (configCount % configsPerRow == 0) {
                        configX = posx + 10;
                        configY += buttonHeight + 5;
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No local configurations available.", configX, configY, applyTextColor(alpha));
            }
        } else {
            if (loadingRemoteSettings) {
                Fonts.InterBold_26.drawString("Loading online configurations...", configX, configY, applyTextColor(alpha));
                return;
            }

            if (remoteError != null) {
                Fonts.InterBold_26.drawString("Error: " + remoteError, configX, configY, applyTextColor(alpha));
                return;
            }

            List<RemoteSetting> remoteSnapshot;
            synchronized (remoteSettings) {
                remoteSnapshot = new ArrayList<>(remoteSettings);
            }

            if (!remoteSnapshot.isEmpty()) {
                for (RemoteSetting setting : remoteSnapshot) {
                    drawConfigButton(mx, my, buttonWidth, buttonHeight, configX, configY, () -> loadOnlineConfig(setting.id, setting.name));
                    Fonts.InterBold_26.drawString(setting.name, configX + 5, configY + 5, applyTextColor(alpha));
                    configX += buttonWidth + 10;
                    configCount++;
                    if (configCount % configsPerRow == 0) {
                        configX = posx + 10;
                        configY += buttonHeight + 5;
                    }
                }
            } else {
                Fonts.InterBold_26.drawString("No online configurations available.", configX, configY, applyTextColor(alpha));
            }
        }
    }

    private void drawConfigButton(int mx, int my, float width, float height, float configX, float configY, Runnable action) {
        boolean hovered = RenderUtil.isHovering(configX, configY, width, height, mx, my);
        Color base = NeverloseGui.getInstance().getLight() ? new Color(245, 245, 245) : new Color(50, 50, 50);
        Color hover = NeverloseGui.getInstance().getLight() ? new Color(225, 225, 225) : new Color(70, 70, 70);
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
            if (loadingRemoteSettings) {
                return rowHeight + 5;
            }
            if (remoteError != null) {
                return rowHeight + 5;
            }

            synchronized (remoteSettings) {
                itemCount = remoteSettings.size();
            }
        }
        if (itemCount == 0) {
            return rowHeight + 5;
        }
        int rows = (int) Math.ceil(itemCount / 4.0);
        return rows * rowHeight;
    }

    private void ensureRemoteSettingsLoaded() {
        if (loadingRemoteSettings || !remoteSettings.isEmpty()) {
            return;
        }

        loadingRemoteSettings = true;
        remoteError = null;

        Thread fetcher = new Thread(() -> {
            try {
                List<?> remoteList = ClientApi.INSTANCE.getAutoSettingsList();
                List<RemoteSetting> fetched = new ArrayList<>();
                if (remoteList != null) {
                    for (Object autoSetting : remoteList) {
                        fetched.add(new RemoteSetting(getSettingId(autoSetting), getSettingName(autoSetting)));
                    }
                }
                synchronized (remoteSettings) {
                    remoteSettings.clear();
                    remoteSettings.addAll(fetched);
                }
            } catch (Exception e) {
                remoteError = e.getMessage();
            } finally {
                loadingRemoteSettings = false;
            }
        }, "nl-online-config-fetch");
        fetcher.setDaemon(true);
        fetcher.start();
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
        try {
            ClientUtils.INSTANCE.displayChatMessage("Loading configuration: " + configName + "...");
            String configScript = ClientApi.INSTANCE.getSettingsScript("legacy", settingId);
            SettingsUtils.INSTANCE.applyScript(configScript);
            ClientUtils.INSTANCE.displayChatMessage("Configuration " + configName + " loaded successfully!");
        } catch (Exception e) {
            ClientUtils.INSTANCE.displayChatMessage("Error loading configuration: " + e.getMessage());
        }
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

    private int applyTextColor(int alpha) {
        Color base = NeverloseGui.getInstance().getLight()
                ? new Color(30, 30, 30, alpha)
                : new Color(255, 255, 255, alpha);
        return base.getRGB();
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

    private static class RemoteSetting {
        private final String id;
        private final String name;

        private RemoteSetting(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
