/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.impl;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<Entry<String, Object>> getPluginInstances() {
        return Imanity.PLUGINS.stream()
                .map(plugin -> new Entry<>(plugin.getName(), (Object) plugin))
                .collect(Collectors.toList());
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        Set<ClassLoader> classLoaders = new HashSet<>();
        classLoaders.add(ImanityCommon.class.getClassLoader());
        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            classLoaders.add(plugin.getClass().getClassLoader());
        }
        return classLoaders;
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
        return Imanity.IMPLEMENTATION.isServerThread();
    }
}
