package org.imanity.framework.mysql.connection.hikari;

import com.zaxxer.hikari.HikariConfig;

import java.util.function.Function;

public class MariaConnectionFactory extends HikariConnectionFactory {
    @Override
    public String defaultPort() {
        return "3306";
    }

    @Override
    public String name() {
        return "MariaDB";
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
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace("'", "`"); // use backticks for quotes
    }
}
