package org.imanity.framework.data.type.impl;

import org.imanity.framework.util.builder.SQLColumnType;

public class BooleanDataConverter extends AbstractDataConverter<Boolean> {

    private boolean b;

    @Override
    public Boolean get() {
        return b;
    }

    @Override
    public void set(Object object) {
        if (object instanceof Boolean) {
            this.b = (boolean) object;
            return;
        }
        if (object instanceof Integer) {
            this.b = ((int) object) == 1;
            return;
        }
        if (object instanceof String) {
            this.b = Boolean.parseBoolean((String) object);
            return;
        }
        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to boolean");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.TINYINT;
    }

    @Override
    public String toStringData(boolean sql) {
        return sql ? String.valueOf(b ? 1 : 0) : super.toStringData(false);
    }
}
