package org.imanity.framework.mysql.connection.file;

import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.classloader.IsolatedClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;

public class H2ConnectionFactory extends FileConnectionFactory {

    private static boolean LIBRARY_LOADED = false;
    private static IsolatedClassLoader CLASS_LOADER;

    private final Driver driver;
    private NonClosableConnection connection;

    public H2ConnectionFactory(Path path) {
        super(path);

        if (!LIBRARY_LOADED) {
            LIBRARY_LOADED = true;

            Library library = new Library(
                    "com.h2database",
                    "h2",
                    // seems to be a compat bug in 1.4.200 with older dbs
                    // see: https://github.com/h2database/h2database/issues/2078
                    "1.4.199",
                    "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="
            );

            Library byteBuddy = new Library(
                    "net.bytebuddy",
                    "byte-buddy",
                    "1.10.9",
                    "B7nKbi+XDLA/SyVlHfHy/OJx1JG0TgQJgniHeG9pLU0="
            );

            FrameworkMisc.LIBRARY_HANDLER.downloadLibraries(library, byteBuddy);
            CLASS_LOADER = FrameworkMisc.LIBRARY_HANDLER.obtainClassLoaderWith(library, byteBuddy);
        }

        try {
            Class<?> driverClass = CLASS_LOADER.loadClass("org.h2.Driver");
            Method loadMethod = driverClass.getMethod("load");
            this.driver = (Driver) loadMethod.invoke(null);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String name() {
        return "H2";
    }

    @Override
    public void connect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return;
        }

        Connection connection = this.driver.connect("jdbc:h2:" + this.path.toString(), new Properties());
        if (connection != null) {
            this.connection = NonClosableConnection.wrap(connection);
        } else {
            throw new SQLException("Unable to get a connection.");
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (this.connection != null) {
            this.connection.shutdown();
        }
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace("'", "`");
    }

    @Override
    public Connection connection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connect();
        }

        return this.connection;
    }
}
