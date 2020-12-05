package org.imanity.framework.mysql.connection.hikari;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.mysql.connection.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class HikariConnectionFactory implements ConnectionFactory {

    @Getter
    protected HikariConfig config;
    protected HikariDataSource dataSource;

    public abstract String defaultPort();

    public abstract void configureDatabase(String address, String port, String databaseName, String username, String password);

    protected void postInitialize() {

    }

    @Override
    public void init() {
        try {
            Class.forName("com.zaxxer.hikari.HikariConfig");
        } catch (ClassNotFoundException ex) {
            Library library = new Library(
                    "com.zaxxer",
                    "HikariCP",
                    "3.1.0",
                    "TBo58lIW2Ukyh3VYKUwOliccAeRx+y9FxdDzsD8UUUw="
            );

            FrameworkMisc.LIBRARY_HANDLER.downloadLibraries(library);
            FrameworkMisc.LIBRARY_HANDLER.obtainClassLoaderWith(library);
        }

        try {
            this.config = new HikariConfig();
        } catch (LinkageError ex) {
            handleLinkageError(ex);
            throw ex;
        }

        this.config.setPoolName("imanity-hikari");
        this.config.setInitializationFailTimeout(-1);
        this.config.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
    }

    @Override
    public void connect() {
        this.dataSource = new HikariDataSource(this.config);

        this.postInitialize();
    }

    @Override
    public void shutdown() {
        if (this.dataSource != null) {
            this.dataSource.close();
        }
    }

    @Override
    public Connection connection() throws SQLException {
        if (this.dataSource == null) {
            throw new SQLException("Unable to get a connection from the pool. (dataSource is null)");
        }

        Connection connection = this.dataSource.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (dataSource.getConnection() returned null)");
        }

        return connection;
    }

    private static void handleLinkageError(LinkageError linkageError) {
        List<String> noteworthyClasses = ImmutableList.of(
                "org.slf4j.LoggerFactory",
                "org.slf4j.ILoggerFactory",
                "org.apache.logging.slf4j.Log4jLoggerFactory",
                "org.apache.logging.log4j.spi.LoggerContext",
                "org.apache.logging.log4j.spi.AbstractLoggerAdapter",
                "org.slf4j.impl.StaticLoggerBinder"
        );

        Logger logger = FrameworkMisc.PLATFORM.getLogger();
        logger.warn("A " + linkageError.getClass().getSimpleName() + " has occurred whilst initialising Hikari. This is likely due to classloading conflicts between other plugins.");
        logger.warn("Please check for other plugins below (and try loading LuckPerms without them installed) before reporting the issue.");

        for (String className : noteworthyClasses) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (Exception e) {
                continue;
            }

            ClassLoader loader = clazz.getClassLoader();
            String loaderName;
            try {
                loaderName = FrameworkMisc.PLATFORM.identifyClassLoader(loader) + " (" + loader.toString() + ")";
            } catch (Throwable e) {
                loaderName = loader.toString();
            }

            logger.warn("Class " + className + " has been loaded by: " + loaderName);
        }
    }
}
