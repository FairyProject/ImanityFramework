package org.imanity.framework;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface ImanityBridge {

    void saveResources(String name, boolean replace);

    File getDataFolder();

    Logger getLogger();

    Map<String, Object> loadYaml(File file);

    Map<String, Object> loadYaml(InputStream inputStream);

    boolean isShuttingDown();

}
