package org.imanity.framework.mysql.config.file;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.file.H2ConnectionFactory;

import java.nio.file.Path;

public abstract class SimpleFileConfiguration extends AbstractSqlConfiguration<H2ConnectionFactory> {

    @Override
    public Class<H2ConnectionFactory> factoryClass() {
        return H2ConnectionFactory.class;
    }

    @Override
    public H2ConnectionFactory factory() {
        return new H2ConnectionFactory(this.path());
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.H2;
    }

    public abstract Path path();
}
