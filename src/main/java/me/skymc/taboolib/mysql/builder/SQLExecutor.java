package me.skymc.taboolib.mysql.builder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class SQLExecutor {

    public static void freeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ignored) {
        }
    }

    public static void freeStatement(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception ignored) {
        }
    }
}
