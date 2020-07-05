package me.skymc.taboolib.mysql.builder;

import com.ilummc.tlib.util.Strings;
import me.skymc.taboolib.mysql.builder.query.RunnableQuery;
import me.skymc.taboolib.mysql.builder.query.RunnableUpdate;
import me.skymc.taboolib.string.ArrayUtils;

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