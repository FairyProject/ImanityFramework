package org.imanity.framework.mysql.connection.hikari;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.function.Function;

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
    public String name() {
        return "MySQL";
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

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace("'", "`"); // use backticks for quotes
    }
}
