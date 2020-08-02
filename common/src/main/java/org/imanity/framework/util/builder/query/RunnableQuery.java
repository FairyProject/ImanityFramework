package org.imanity.framework.util.builder.query;

import org.imanity.framework.util.builder.SQLExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class RunnableQuery {

	private DataSource dataSource;
	private TaskStatement statement;
	private TaskResult result;
	private TaskResult resultNext;
	private Connection connection;
	private boolean autoClose;
	private final String query;

	public RunnableQuery(String query) {
		this.query = query;
	}

	public RunnableQuery dataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public RunnableQuery statement(TaskStatement statement) {
		this.statement = statement;
		return this;
	}

	public RunnableQuery result(TaskResult result) {
		this.result = result;
		return this;
	}

	public RunnableQuery resultNext(TaskResult result) {
		this.resultNext = result;
		return this;
	}

	public RunnableQuery connection(Connection connection) {
		return connection(connection, false);
	}

	public RunnableQuery connection(Connection connection, boolean autoClose) {
		this.connection = connection;
		this.autoClose = autoClose;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T run(Object def, T translate) {
		final Object object = run(def);
		return object == null ? def == null ? null : (T) def : (T) object;
	}

	public Object run() {
		return run(null);
	}

	public Object run(Object def) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		if (dataSource != null) {
			try (Connection connection = dataSource.getConnection()) {
				preparedStatement = connection.prepareStatement(query);
				if (statement != null) {
					statement.execute(preparedStatement);
				}
				resultSet = preparedStatement.executeQuery();
				return getResult(resultSet);
			} catch (final Exception e) {
				printException(e);
			} finally {
				SQLExecutor.freeStatement(preparedStatement, resultSet);
			}
		} else if (connection != null) {
			try {
				preparedStatement = connection.prepareStatement(query);
				if (statement != null) {
					statement.execute(preparedStatement);
				}
				resultSet = preparedStatement.executeQuery();
				return getResult(resultSet);
			} catch (final Exception e) {
				printException(e);
			} finally {
				SQLExecutor.freeStatement(preparedStatement, resultSet);
				if (autoClose) {
					SQLExecutor.freeConnection(connection);
				}
			}
		}
		return def;
	}

	private void printException(Exception e) {
		e.printStackTrace();
	}

	private Object getResult(ResultSet resultSet) throws SQLException {
		if (resultNext != null && resultSet.next()) {
			return resultNext.execute(resultSet);
		} else if (result != null) {
			return result.execute(resultSet);
		} else {
			return null;
		}
	}
}