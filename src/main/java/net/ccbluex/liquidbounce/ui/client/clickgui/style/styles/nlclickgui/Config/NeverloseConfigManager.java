package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class NeverloseConfigManager {

    private final List<NeverloseConfig> configs = new ArrayList<>();

    public NeverloseConfigManager() {
        refresh();
    }

    public List<NeverloseConfig> getConfigs() {
        if (configs.isEmpty()) {
            refresh();
        }
        return configs;
    }

    public NeverloseConfig getActiveConfig() {
        final String active = FDPClient.fileManager.getNowConfig();
        return configs.stream()
                .filter(config -> config.getName().equalsIgnoreCase(active))
                .findFirst()
                .orElse(null);
    }

    public void refresh() {
        configs.clear();
        final File[] configFiles = FDPClient.fileManager.getSettingsDir().listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles != null) {
            for (File file : configFiles) {
                configs.add(new NeverloseConfig(removeExtension(file.getName()), file));
            }
            configs.sort(Comparator.comparing(NeverloseConfig::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    public void toggleExpansion(NeverloseConfig config) {
        config.setExpanded(!config.isExpanded());
    }

    public void loadConfig(String name) {
        FDPClient.fileManager.load(name);
        refresh();
    }

    public void saveConfig(String name) {
        FDPClient.fileManager.load(name, false);
        FDPClient.fileManager.saveAllConfigs();
        refresh();
    }

    public void deleteConfig(NeverloseConfig config) {
        final File file = config.getFile();
        if (file.exists() && !file.delete()) {
            ClientUtils.LOGGER.warn("Failed to delete config file: {}", file.getName());
        }
        if (Objects.equals(FDPClient.fileManager.getNowConfig(), config.getName())) {
            FDPClient.fileManager.load("default", false);
            FDPClient.fileManager.saveAllConfigs();
        }
        refresh();
    }

    public NeverloseConfig ensureConfig(String name) {
        File file = new File(FDPClient.fileManager.getSettingsDir(), name + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FDPClient.fileManager.load(name, false);
                FDPClient.fileManager.saveAllConfigs();
            } catch (IOException e) {
                ClientUtils.LOGGER.error("Failed to create config {}", name, e);
            }
            refresh();
        }
        return configs.stream()
                .filter(config -> config.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    NeverloseConfig config = new NeverloseConfig(name, file);
                    configs.add(config);
                    return config;
                });
    }

    private String removeExtension(String name) {
        final int dotIndex = name.lastIndexOf('.');
        return dotIndex == -1 ? name : name.substring(0, dotIndex);
    }
}
