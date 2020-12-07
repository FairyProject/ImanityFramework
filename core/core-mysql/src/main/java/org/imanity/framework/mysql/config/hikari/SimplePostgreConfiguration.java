package org.imanity.framework.mysql.config.hikari;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.connection.hikari.PostgreConnectionFactory;

public abstract class SimplePostgreConfiguration extends SimpleHikariConfiguration<PostgreConnectionFactory> {

    @Override
    public Class<PostgreConnectionFactory> factoryClass() {
        return PostgreConnectionFactory.class;
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.POSTGRE;
    }
}
