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

package org.imanity.framework.bungee.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.libraries.classloader.PluginClassLoader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BungeeImanityBridge implements ImanityBridge {
    @Override
    public void saveResources(String resourcePath, boolean replace) {
        File dataFolder = Imanity.PLUGIN.getDataFolder();
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in Mitw.jar");
            } else {
                File outFile = new File(dataFolder, resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                        ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    ProxyServer.getInstance().getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    private InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = Imanity.PLUGIN.getClass().getClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException var4) {
                return null;
            }
        }
    }

    @Override
    public PluginClassLoader getClassLoader() {
        return Imanity.CLASS_LOADER;
    }

    @Override
    public File getDataFolder() {
        return Imanity.PLUGIN.getDataFolder();
    }

    @Override
    public org.apache.logging.log4j.Logger getLogger() {
        return Imanity.LOGGER;
    }

    @Override
    @SneakyThrows
    public Map<String, Object> loadYaml(File file) {
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (String key : configuration.getKeys()) {
            builder.put(key, configuration.get(key));
        }

        return builder.build();
    }

    @Override
    public Map<String, Object> loadYaml(InputStream inputStream) {
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(inputStream);
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (String key : configuration.getKeys()) {
            builder.put(key, configuration.get(key));
        }

        return builder.build();
    }

    @Override
    public List<Object> getPluginInstances() {
        return ImmutableList.copyOf(Imanity.PLUGINS);
    }

    @Override
    public List<File> getPluginFiles() {
        return Imanity.PLUGINS.stream()
                .map(Plugin::getFile)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isShuttingDown() {
        return Imanity.SHUTTING_DOWN;
    }

    @Override
    public boolean isServerThread() {
        return true; // BungeeCord is async
    }
}
