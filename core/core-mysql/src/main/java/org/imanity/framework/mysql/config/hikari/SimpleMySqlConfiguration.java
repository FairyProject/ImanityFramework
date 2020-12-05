package org.imanity.framework.mysql.config.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.hikari.MariaConnectionFactory;
import org.imanity.framework.mysql.connection.hikari.MySqlConnectionFactory;

import java.util.concurrent.TimeUnit;

public abstract class SimpleMySqlConfiguration extends SimpleHikariConfiguration<MySqlConnectionFactory> {

    @Override
    public Class<MySqlConnectionFactory> factoryClass() {
        return MySqlConnectionFactory.class;
    }

    @Override
    public void setupFactory(MySqlConnectionFactory factory) {
        super.setupFactory(factory);
        HikariConfig config = factory.getConfig();

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
    }

    public abstract String address();

    public abstract String port();

    public abstract String databaseName();

    public abstract String username();

    public abstract String password();
}
