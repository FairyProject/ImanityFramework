package org.imanity.framework.mysql.config.hikari;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.connection.hikari.MariaConnectionFactory;

public abstract class SimpleMariaConfiguration extends SimpleHikariConfiguration<MariaConnectionFactory> {

    @Override
    public Class<MariaConnectionFactory> factoryClass() {
        return MariaConnectionFactory.class;
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.MARIADB;
    }
}
