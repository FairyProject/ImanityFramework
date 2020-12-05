package org.imanity.framework.mysql.config;

import org.imanity.framework.mysql.connection.ConnectionFactory;

public abstract class AbstractSqlConfiguration<T extends ConnectionFactory> {

    public abstract Class<T> factoryClass();

    public abstract T factory();

}
