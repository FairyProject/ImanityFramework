package org.imanity.framework.mysql.connection.file;

import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.RepositoryType;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.classloader.IsolatedClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class H2ConnectionFactory extends FileConnectionFactory {

    private static boolean LIBRARY_LOADED = false;
    private static IsolatedClassLoader CLASS_LOADER;

    private final Driver driver;
    private NonClosableConnection connection;

    public H2ConnectionFactory(Path path) {
        this(path, false);
    }

    public H2ConnectionFactory(Path path, boolean test) {
        super(path);

        try {
            if (test) {
                Class<?> driverClass = Class.forName("org.h2.Driver");
                Method loadMethod = driverClass.getMethod("load");
                this.driver = (Driver) loadMethod.invoke(null);
                return;
            }
            Class<?> driverClass = FrameworkMisc.LIBRARY_HANDLER
                    .obtainClassLoaderWith(Library.H2_DRIVER)
                    .loadClass("org.h2.Driver");
            Method loadMethod = driverClass.getMethod("load");
            this.driver = (Driver) loadMethod.invoke(null);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public RepositoryType type() {
        return RepositoryType.H2;
    }

    @Override
    public void connect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        Connection connection = this.driver.connect(this.url(), new Properties());
        if (connection != null) {
            this.connection = NonClosableConnection.wrap(connection);
        } else {
            throw new SQLException("Unable to get a connection.");
        }
    }

    public String url() {
        return "jdbc:h2:" + this.path.toString() + ";mode=MySQL";
    }

    @Override
    public void shutdown() throws Exception {
        if (this.connection != null) {
            this.connection.shutdown();
        }
    }

    @Override
    public Connection connection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connect();
        }

        return this.connection;
    }
}