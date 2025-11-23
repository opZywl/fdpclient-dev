package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Config;

import java.io.File;
import java.util.Objects;

public class NeverloseConfig {
    private final String name;
    private final File file;
    private boolean expanded;

    public NeverloseConfig(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getAuthor() {
        return "Local";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeverloseConfig that = (NeverloseConfig) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
