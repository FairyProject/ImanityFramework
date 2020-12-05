package org.imanity.framework.mysql.connection.file;

import lombok.Getter;
import org.imanity.framework.mysql.connection.ConnectionFactory;

import java.nio.file.Path;
import java.text.DecimalFormat;

abstract class FileConnectionFactory implements ConnectionFactory {

    protected static final DecimalFormat DF = new DecimalFormat("#.##");

    @Getter
    protected final Path path;

    FileConnectionFactory(Path path) {
        this.path = path;
    }

    @Override
    public void init() {

    }
}
