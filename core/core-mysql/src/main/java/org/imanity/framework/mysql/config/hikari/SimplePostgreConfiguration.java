package org.imanity.framework.mysql.config.hikari;

import org.imanity.framework.mysql.connection.hikari.PostgreConnectionFactory;

public abstract class SimplePostgreConfiguration extends SimpleHikariConfiguration<PostgreConnectionFactory> {

    @Override
    public Class<PostgreConnectionFactory> factoryClass() {
        return PostgreConnectionFactory.class;
    }
}
