package org.imanity.framework.player.data.type.impl;

import com.google.gson.JsonElement;
import lombok.NoArgsConstructor;
import me.skymc.taboolib.mysql.builder.SQLColumnType;

public class BooleanData extends AbstactData<java.lang.Boolean> {

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
