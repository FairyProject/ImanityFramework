package org.imanity.framework.mysql.connection.hikari;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.pojo.statement.MySqlStatementBuilder;
import org.imanity.framework.mysql.pojo.statement.SqlStatementBuilder;

public class MariaConnectionFactory extends HikariConnectionFactory {
    @Override
    public String defaultPort() {
        return "3306";
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.MARIADB;
    }

    @Override
    public void configureDatabase(String address, String port, String databaseName, String username, String password) {
        this.config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        this.config.addDataSourceProperty("serverName", address);
        this.config.addDataSourceProperty("port", port);
        this.config.addDataSourceProperty("databaseName", databaseName);
        this.config.setUsername(username);
        this.config.setPassword(password);
    }

    @Override
    public SqlStatementBuilder builder() {
        return new MySqlStatementBuilder();
    }

}