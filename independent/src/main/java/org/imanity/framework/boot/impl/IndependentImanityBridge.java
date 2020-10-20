package org.imanity.framework.boot.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.util.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class IndependentImanityBridge implements ImanityBridge {

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
    public Map<String, Object> loadYaml(File file) {
        try {
            return this.bootable.getYaml().load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            this.bootable.handleError(e);
            return null;
        }
    }

    @Override
    public Map<String, Object> loadYaml(InputStream inputStream) {
        return this.bootable.getYaml().load(inputStream);
    }

    @Override
    public List<Object> getPluginInstances() {
        return Collections.singletonList(this.bootable.getBootableObject());
    }

    @Override
    public List<File> getPluginFiles() {
        try {
            return Collections.singletonList(FileUtils.getSelfJar());
        } catch (URISyntaxException e) {
            this.bootable.handleError(e);
            return null;
        }
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
