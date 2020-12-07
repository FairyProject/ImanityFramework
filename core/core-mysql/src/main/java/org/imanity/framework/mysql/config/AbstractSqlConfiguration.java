package org.imanity.framework.mysql.config;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.connection.AbstractConnectionFactory;

public abstract class AbstractSqlConfiguration<T extends AbstractConnectionFactory> {

    public abstract Class<T> factoryClass();

    public abstract T factory();

    public abstract RepositoryType type();

    public boolean shouldActivate() {
        return true;
    }

}
