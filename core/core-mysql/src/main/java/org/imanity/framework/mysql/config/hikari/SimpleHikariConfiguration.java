package org.imanity.framework.mysql.config.hikari;

import com.zaxxer.hikari.HikariConfig;
import lombok.SneakyThrows;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.hikari.HikariConnectionFactory;

public abstract class SimpleHikariConfiguration<T extends HikariConnectionFactory> extends AbstractSqlConfiguration<T> {

    @Override
    @SneakyThrows
    public T factory() {
        T factory = this.factoryClass().newInstance();
        factory.init();
        this.setupFactory(factory);
        return factory;
    }

    public void setupFactory(T factory) {
        factory.configureDatabase(this.address(), this.port(), this.databaseName(), this.username(), this.password());
        HikariConfig config = factory.getConfig();

        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        config.setValidationTimeout(3000);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(60000);
    }

    public abstract String address();

    public abstract String port();

    public abstract String databaseName();

    public abstract String username();

    public abstract String password();

}
