package org.imanity.framework.player.data.type;

import org.imanity.framework.util.builder.SQLColumnType;

import java.lang.reflect.Field;

public interface Data<T> {

    String name();

    Object get();

    void set(Object object);

    void setName(String name);

    SQLColumnType columnType();

    String toStringData(boolean sql);

    Object toFieldObject(Field field);

    Class<?> getLoadType();

    Class<?> getType();

}
