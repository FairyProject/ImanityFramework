package org.imanity.framework.mysql.connection;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public interface ConnectionFactory {

    String name();

    void init();

    void connect() throws SQLException;

    @SneakyThrows
    void shutdown() throws Exception;

    Function<String, String> getStatementProcessor();

    Connection connection() throws SQLException;
}
