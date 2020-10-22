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

package org.imanity.framework.util.builder;

import org.imanity.framework.util.builder.query.RunnableQuery;
import org.imanity.framework.util.builder.query.RunnableUpdate;
import org.imanity.framework.util.string.ArrayUtils;
import org.imanity.framework.util.Strings;

import java.util.Arrays;


public class SQLTable {

    private String tableName;
    private SQLColumn[] columns;

    public SQLTable(String tableName) {
        this.tableName = tableName;
    }

    public SQLTable(String tableName, SQLColumn... column) {
        this.tableName = tableName;
        this.columns = column;
    }

    public SQLTable addColumn(SQLColumn sqlColumn) {
        if (columns == null) {
            columns = new SQLColumn[] {sqlColumn};
        } else {
            columns = ArrayUtils.arrayAppend(columns, sqlColumn);
        }
        return this;
    }

    public String createQuery() {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(columns).forEach(sqlColumn -> builder.append(sqlColumn.convertToCommand()).append(", "));
        return Strings.replaceWithOrder("CREATE TABLE IF NOT EXISTS `{0}` ({1})", tableName, builder.substring(0, builder.length() - 2));
    }

    public String deleteQuery() {
        return Strings.replaceWithOrder("drop table if exists `{0}`" + tableName);
    }

    public String cleanQuery() {
        return Strings.replaceWithOrder("DELETE FROM `{0}`" + tableName);
    }

    public String truncateQuery() {
        return Strings.replaceWithOrder("TRUNCATE TABLE `{0}`", tableName);
    }

    public RunnableUpdate executeInsert(String values) {
        return executeUpdate("INSERT INTO " + tableName + " VALUES(" + values + ")");
    }

    public RunnableQuery executeSelect(String where) {
        return executeQuery("SELECT * FROM " + tableName + " WHERE " + where);
    }

    public RunnableUpdate executeUpdate(String update, String where) {
        return executeUpdate("UPDATE " + tableName + " SET " + update + " WHERE " + where);
    }

    public RunnableUpdate executeUpdate(String query) {
        return new RunnableUpdate(query);
    }

    public RunnableQuery executeQuery(String query) {
        return new RunnableQuery(query);
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public String getTableName() {
        return tableName;
    }

    public SQLColumn[] getColumns() {
        return columns;
    }
}