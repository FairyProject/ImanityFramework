package org.imanity.framework.data.type.impl;

import org.imanity.framework.data.type.DataConverter;
import org.imanity.framework.util.builder.SQLColumnType;

import java.lang.reflect.Field;
import java.util.UUID;

public class UUIDDataConverter extends AbstractDataConverter<UUID> {

    private UUID uuid;

    @Override
    public Object get() {
        return uuid.toString();
    }

    @Override
    public void set(Object object) {
        if (object instanceof UUID) {
            this.uuid = (UUID) object;
        } else if (object instanceof String) {
            this.uuid = UUID.fromString((String) object);
        }
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.TEXT;
    }

    @Override
    public Object toFieldObject(Field field) {
        return uuid;
    }

    @Override
    public Class<?> getLoadType() {
        return String.class;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + uuid.toString() + (sql ? "\"" : "");
    }
}
