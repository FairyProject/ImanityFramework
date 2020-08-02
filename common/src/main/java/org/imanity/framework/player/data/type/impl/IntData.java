package org.imanity.framework.player.data.type.impl;

import org.imanity.framework.util.builder.SQLColumnType;

import java.lang.reflect.Field;

public class IntData extends AbstractData<Integer> {

    private int i;

    @Override
    public Integer get() {
        return this.i;
    }

    @Override
    public void set(Object object) {
        if (object instanceof Integer) {
            this.i = (Integer) object;
            return;
        } else if (object instanceof Short) {
            this.i = ((Short) object).intValue();
            return;
        } else if (object instanceof String) {
            this.i = Integer.parseInt((String) object);
            return;
        }
        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to integer or short");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.INT;
    }

    @Override
    public Object toFieldObject(Field field) {
        if (field.getType() == int.class) {
            return i;
        } else if (field.getType() == short.class) {
            return Integer.valueOf(i).shortValue();
        }
        return null;
    }
}
