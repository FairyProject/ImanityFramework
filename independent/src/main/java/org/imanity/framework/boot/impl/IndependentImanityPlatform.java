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

package org.imanity.framework.boot.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityPlatform;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.plugin.PluginClassLoader;
import org.imanity.framework.util.entry.Entry;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

@RequiredArgsConstructor
public class IndependentImanityPlatform implements ImanityPlatform {

    private final FrameworkBootable bootable;

    @Override
    public void saveResources(String resourcePath, boolean replace) {
        File dataFolder = this.getDataFolder();
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
                        FrameworkBootable.LOGGER.warn("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
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
                    FrameworkBootable.LOGGER.info("Could not save " + outFile.getName() + " to " + outFile, var10);
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
                URL url = FrameworkBootable.class.getClassLoader().getResource(filename);
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
        return this.bootable.getPluginClassLoader();
    }

    @Override
    public File getDataFolder() {
        return new File(".");
    }

    @Override
    public Logger getLogger() {
        return FrameworkBootable.LOGGER;
    }

    @Override
    public List<Entry<String, Object>> getPluginInstances() {
        return Arrays.asList(
                new Entry<>("bootable", this.bootable),
                new Entry<>("independent", this.bootable.getBootableObject())
        );
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        return Collections.singleton(this.bootable.getBootableClass().getClassLoader());
    }

    @Override
    public List<File> getPluginFiles() {
        return new ArrayList<>();
    }

    @Override
    public boolean isShuttingDown() {
        return this.bootable.isShuttingDown();
    }

    @Override
    public boolean isServerThread() { // ASYNC?
        return true;
    }
}
