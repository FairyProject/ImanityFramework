package me.skymc.taboolib.mysql.builder.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface TaskStatement {

    void execute(PreparedStatement preparedStatement) throws SQLException;

}
