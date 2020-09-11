package org.imanity.framework;

import org.apache.logging.log4j.Logger;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.task.ITaskScheduler;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ImanityBridge {

    void saveResources(String name, boolean replace);

    PluginClassLoader getClassLoader();

    File getDataFolder();

    Logger getLogger();

    Map<String, Object> loadYaml(File file);

    Map<String, Object> loadYaml(InputStream inputStream);

    List<Object> getPluginInstances();

    List<File> getPluginFiles();

    boolean isShuttingDown();

    boolean isServerThread();

}
