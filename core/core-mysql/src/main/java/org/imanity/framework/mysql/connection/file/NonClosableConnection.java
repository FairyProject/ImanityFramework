package org.imanity.framework.mysql.connection.file;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.sql.Connection;
import java.sql.SQLException;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isFinal;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * A wrapper around a {@link Connection} which blocks usage of the default {@link #close()} method.
 */
public abstract class NonClosableConnection implements Connection {

    private static final MethodHandle CONSTRUCTOR;
    static {
        // construct an implementation of NonClosableConnection
        Class<? extends NonClosableConnection> implClass = new ByteBuddy()
                .subclass(NonClosableConnection.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(NonClosableConnection.class.getName() + "Impl")
                .method(not(isFinal()).and(not(isDeclaredBy(Object.class))))
                .intercept(MethodDelegation.toField("delegate"))
                .make()
                .load(NonClosableConnection.class.getClassLoader())
                .getLoaded();

        try {
            CONSTRUCTOR = MethodHandles.publicLookup().in(implClass)
                    .findConstructor(implClass, MethodType.methodType(void.class, Connection.class))
                    .asType(MethodType.methodType(NonClosableConnection.class, Connection.class));
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Creates a {@link NonClosableConnection} that delegates calls to the given {@link Connection}.
     *
     * @param connection the connection to wrap
     * @return a non closable connection
     */
    static NonClosableConnection wrap(Connection connection) {
        try {
            return (NonClosableConnection) CONSTRUCTOR.invokeExact(connection);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected final Connection delegate;

    protected NonClosableConnection(Connection delegate) {
        this.delegate = delegate;
    }

    /**
     * Actually {@link #close() closes} the underlying connection.
     */
    public final void shutdown() throws SQLException {
        this.delegate.close();
    }

    @Override
    public final void close() throws SQLException {
        // do nothing
    }

    @Override
    public final boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this.delegate) || this.delegate.isWrapperFor(iface);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this.delegate)) {
            return (T) this.delegate;
        }
        return this.delegate.unwrap(iface);
    }
}