package org.imanity.framework.bukkit.impl;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.libraries.classloader.PluginClassLoader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<Object> getPluginInstances() {
        return ImmutableList.copyOf(Imanity.PLUGINS);
    }

    @Override
    public List<File> getPluginFiles() {
        return Imanity.PLUGINS
                .stream()
                .map(BukkitUtil::getPluginJar)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isServerThread() {
        return SpigotUtil.isServerThread();
    }
}
