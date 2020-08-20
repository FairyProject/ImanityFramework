package org.imanity.framework.data.store.impl;

import org.imanity.framework.data.AbstractData;
import org.imanity.framework.util.builder.SQLColumn;
import org.imanity.framework.util.builder.SQLColumnOption;
import org.imanity.framework.util.builder.SQLColumnType;
import org.imanity.framework.util.builder.SQLTable;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.type.DataConverter;
import org.redisson.api.RReadWriteLock;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SQLDatabase extends AbstractDatabase {

    private String tableName;
    private SQLTable table;

    @Override
    public void init(String name) {
        this.tableName = ImanityCommon.SQL.getConfig().TABLE_PREFIX + name;
        this.table = new SQLTable(this.tableName)
            .addColumn(new SQLColumn(SQLColumnType.TEXT, "uuid", SQLColumnOption.NOTNULL, SQLColumnOption.PRIMARY_KEY))
            .addColumn(new SQLColumn(SQLColumnType.TINYTEXT, "name"));

        this.getFieldConverters().forEach(type -> {
            DataConverter<?> emptyData = type.getType().newData();

            this.table.addColumn(new SQLColumn(emptyData.columnType(), type.getName()));
        });

        this.table.executeUpdate(this.table.createQuery()).dataSource(ImanityCommon.SQL.getDataSource()).run();
    }

    @Override
    public void load(AbstractData playerData) {
        RReadWriteLock lock = null;
        if (ImanityCommon.CORE_CONFIG.USE_REDIS_DISTRIBUTED_LOCK) {
            lock = ImanityCommon.REDIS.getLock("Data-" + playerData.getUuid());
            lock.readLock().lock(3L, TimeUnit.SECONDS);
        }

        this.table.executeSelect("WHERE uuid = ?")
                .dataSource(ImanityCommon.SQL.getDataSource())
                .statement(s -> {
                    s.setString(1, playerData.getUuid().toString());
                }).result(result -> {
            if (result.isBeforeFirst()) {
                result.next();
                this.getDataConverterTypes().forEach((type) -> {
                    try {
                        type.getField().set(playerData, result.getObject(type.getName()));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
            return null;
        }).run();

        if (lock != null) {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(AbstractData playerData) {
        List<DataConverter<?>> dataList = playerData.toDataList(this);

        StringBuilder stringBuilder = new StringBuilder()
                .append("INSERT INTO ")
                .append(tableName)
                .append(" VALUES (");

        Iterator<DataConverter<?>> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next().toStringData(true));

            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(") ON DUPLICATE KEY UPDATE ");
        iterator = dataList.iterator();
        while (iterator.hasNext()) {
            DataConverter<?> data = iterator.next();
            stringBuilder.append(data.name())
                         .append(" = ")
                         .append(data.toStringData(true));

            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(";");

        RReadWriteLock lock = null;
        if (ImanityCommon.CORE_CONFIG.USE_REDIS_DISTRIBUTED_LOCK) {
            lock = ImanityCommon.REDIS.getLock("Data-" + playerData.getUuid());
            lock.writeLock().lock(3L, TimeUnit.SECONDS);
        }

        this.table.executeUpdate(stringBuilder.toString())
                .dataSource(ImanityCommon.SQL.getDataSource())
                .run();

        if (lock != null) {
            lock.writeLock().unlock();
        }
    }
}
