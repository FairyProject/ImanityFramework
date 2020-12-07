package org.imanity.framework.mysql.pojo;

import org.imanity.framework.mysql.ImanitySqlException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

/**
 * Represents a database transaction. Create it using Transaction trans =
 * Database.startTransation(), pass it to the query object using
 * .transaction(trans), and then call trans.commit() or trans.rollback().
 * <p>
 * Some things to note: commit() and rollback() also call close() on the
 * connection, so this class cannot be reused after the transaction is committed
 * or rolled back.
 * </p>
 * <p>
 * This is just a convenience class. If the implementation is too restrictive,
 * then you can manage your own transactions by calling Database.getConnection()
 * and operate on the Connection directly.
 * </p>
 */
public class Transaction implements Closeable {
	private Connection connection;

	public void setConnection(Connection con) {
		this.connection = con;
		try {
			con.setAutoCommit(false);
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		}
	}

	public void commit() {
		try {
			connection.commit();
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		} finally {
			try {
				connection.close();
			} catch (Throwable t) {
				throw new ImanitySqlException(t);
			}
		}
	}

	public void rollback() {
		try {
			connection.rollback();
		} catch (Throwable t) {
			throw new ImanitySqlException(t);
		} finally {
			try {
				connection.close();
			} catch (Throwable t) {
				throw new ImanitySqlException(t);
			}
		}
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * This simply calls .commit();
	 */
	@Override
	public void close() throws IOException {
		commit();
	}

}
