package org.imanity.framework.player.data.type.impl;

import com.google.gson.JsonElement;
import lombok.NoArgsConstructor;
import me.skymc.taboolib.mysql.builder.SQLColumnType;

@NoArgsConstructor
public class StringData extends AbstactData<String> {

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
