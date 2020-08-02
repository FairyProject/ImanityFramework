package org.imanity.framework.player.data.type.impl;

import lombok.NoArgsConstructor;
import org.imanity.framework.util.builder.SQLColumnType;

@NoArgsConstructor
public class StringData extends AbstractData<String> {

    private String s;

    @Override
    public String get() {
        return s;
    }

    @Override
    public void set(Object object) {
        this.s = object.toString();
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.TEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + s + (sql ? "\"" : "");
    }
}
