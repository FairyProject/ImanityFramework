package org.imanity.framework.config;

import org.imanity.framework.Component;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.mysql.config.file.SimpleFileConfiguration;

import java.nio.file.Path;

@Component
public class DefaultH2Configuration extends SimpleFileConfiguration {

    @Override
    public Path path() {
        return ImanityCommon.PLATFORM.getDataFolder()
                .toPath().toAbsolutePath()
                .resolve("imanity-h2");
    }
}
