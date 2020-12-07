package org.imanity.framework.mysql.connection.hikari;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.pojo.statement.PostgresStatementBuilder;
import org.imanity.framework.mysql.pojo.statement.SqlStatementBuilder;

public class PostgreConnectionFactory extends HikariConnectionFactory {
    @Override
    public String defaultPort() {
        return "5432";
    }

    @Override
    public void configureDatabase(String address, String port, String databaseName, String username, String password) {
        this.config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        this.config.addDataSourceProperty("serverName", address);
        this.config.addDataSourceProperty("portNumber", port);
        this.config.addDataSourceProperty("databaseName", databaseName);
        this.config.addDataSourceProperty("user", username);
        this.config.addDataSourceProperty("password", password);
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.POSTGRE;
    }

    @Override
    public SqlStatementBuilder builder() {
        return new PostgresStatementBuilder();
    }

}