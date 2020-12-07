package org.imanity.framework.mysql.connection.hikari;

import org.imanity.framework.RepositoryType;
import org.imanity.framework.mysql.pojo.statement.MySqlStatementBuilder;
import org.imanity.framework.mysql.pojo.statement.SqlStatementBuilder;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class MySqlConnectionFactory extends HikariConnectionFactory {
    @Override
    public String defaultPort() {
        return "3306";
    }

    @Override
    public void configureDatabase(String address, String port, String databaseName, String username, String password) {
        this.config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + databaseName);
        this.config.setUsername(username);
        this.config.setPassword(password);
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.MYSQL;
    }

    @Override
    public SqlStatementBuilder builder() {
        return new MySqlStatementBuilder();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals("com.mysql.cj.jdbc.Driver")) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

}