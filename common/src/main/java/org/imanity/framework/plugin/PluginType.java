package org.imanity.framework.plugin;

import lombok.Getter;

public enum PluginType {

    BUKKIT("plugin.yml"),
    BUNGEE("bungee.yml");

    private final String fileName;

    private PluginType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
