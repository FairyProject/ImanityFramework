package org.imanity.framework.mysql.connection.file;

import lombok.Getter;
import org.imanity.framework.mysql.connection.AbstractConnectionFactory;
import org.imanity.framework.mysql.pojo.statement.MySqlStatementBuilder;
import org.imanity.framework.mysql.pojo.statement.SqlStatementBuilder;
import org.imanity.framework.mysql.pojo.statement.StandardSqlStatementBuilder;

import java.nio.file.Path;
import java.text.DecimalFormat;

abstract class FileConnectionFactory extends AbstractConnectionFactory {

    protected static final DecimalFormat DF = new DecimalFormat("#.##");

    @Getter
    protected final Path path;

    FileConnectionFactory(Path path) {
        this.path = path;
    }

    @Override
    public SqlStatementBuilder builder() {
        return new MySqlStatementBuilder();
    }

    @Override
    public void init() {

    }
}