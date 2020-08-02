package org.imanity.framework.util.builder.query;

import java.sql.ResultSet;
import java.sql.SQLException;


public interface TaskResult {

    Object execute(ResultSet resultSet) throws SQLException;

}
