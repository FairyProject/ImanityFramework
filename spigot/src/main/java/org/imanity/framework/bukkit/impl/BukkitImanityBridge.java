package org.imanity.framework.bukkit.impl;

import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.task.ITaskScheduler;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class BukkitImanityBridge implements ImanityBridge {
    @Override
    public File getDataFolder() {
        return Imanity.PLUGIN.getDataFolder();
    }

    @Override
    public Logger getLogger() {
        return Imanity.LOGGER;
    }

    @Override
    public PluginClassLoader getClassLoader() {
        return Imanity.CLASS_LOADER;
    }

    @Override
    public ITaskScheduler getTaskScheduler() {
        return new BukkitTaskScheduler();
    }

    @Override
    public Map<String, Object> loadYaml(File file) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        return configuration.getValues(true);
    }

    @Override
    public Map<String, Object> loadYaml(InputStream inputStream) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(inputStream);
        return configuration.getValues(true);
    }

    @Override
    public void saveResources(String name, boolean replace) {
        Imanity.PLUGIN.saveResource(name, replace);
    }

    @Override
    public boolean isShuttingDown() {
        return Imanity.SHUTTING_DOWN;
    }
}
