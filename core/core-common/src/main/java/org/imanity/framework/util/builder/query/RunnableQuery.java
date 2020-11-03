/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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