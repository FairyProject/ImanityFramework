package org.imanity.framework.util.builder.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface TaskStatement {

    void execute(PreparedStatement preparedStatement) throws SQLException;

}
