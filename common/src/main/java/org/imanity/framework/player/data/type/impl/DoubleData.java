package org.imanity.framework.player.data.type.impl;

import org.imanity.framework.util.builder.SQLColumnType;

import java.lang.reflect.Field;

public class DoubleData extends AbstractData<Double> {

    private double d;

    @Override
    public Double get() {
        return d;
    }

    @Override
    public void set(Object object) {
        if (object instanceof Double) {
            this.d = (double) object;
            return;
        } else if (object instanceof Float) {
            this.d = ((Float) object).doubleValue();
            return;
        } else if (object instanceof String) {
            this.d = Double.parseDouble((String) object);
            return;
        }
        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to double or float");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.DOUBLE;
    }

    @Override
    public Object toFieldObject(Field field) {
        if (field.getType() == double.class) {
            return d;
        } else if (field.getType() == float.class) {
            return Double.valueOf(d).floatValue();
        }
        return null;
    }
}
