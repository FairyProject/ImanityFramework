package org.imanity.framework.config;

import org.imanity.framework.Component;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.mysql.config.hikari.SimpleMySqlConfiguration;

@Component
public class DefaultSqlConfiguration extends SimpleMySqlConfiguration {

    @Override
    public boolean shouldActivate() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.ENABLED;
    }

    @Override
    public String address() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.HOST;
    }

    @Override
    public String port() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.PORT;
    }

    @Override
    public String databaseName() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.DATABASE;
    }

    @Override
    public String username() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.USER;
    }

    @Override
    public String password() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MYSQL.PASSWORD;
    }
}
