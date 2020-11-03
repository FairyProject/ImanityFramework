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


public class RunnableUpdate {

    private DataSource dataSource;
    private TaskStatement statement;
    private Connection connection;
    private boolean autoClose;
    private String query;

    public RunnableUpdate(String query) {
        this.query = query;
    }

    public RunnableUpdate dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public RunnableUpdate statement(TaskStatement task) {
        this.statement = task;
        return this;
    }

    public RunnableUpdate connection(Connection connection) {
        return connection(connection, false);
    }

    public RunnableUpdate connection(Connection connection, boolean autoClose) {
        this.connection = connection;
        this.autoClose = autoClose;
        return this;
    }

    public void run() {
        PreparedStatement preparedStatement = null;
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                preparedStatement = connection.prepareStatement(query);
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                printException(e);
            } finally {
                SQLExecutor.freeStatement(preparedStatement, null);
            }
        } else if (connection != null) {
            try {
                preparedStatement = connection.prepareStatement(query);
                if (statement != null) {
                    statement.execute(preparedStatement);
                }
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                printException(e);
            } finally {
                SQLExecutor.freeStatement(preparedStatement, null);
                if (autoClose) {
                    SQLExecutor.freeConnection(connection);
                }
            }
        }
    }

    private void printException(Exception e) {
        e.printStackTrace();
    }
}
