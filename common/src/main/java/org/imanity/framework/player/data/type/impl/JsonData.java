package org.imanity.framework.player.data.type.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.imanity.framework.util.builder.SQLColumnType;

@NoArgsConstructor
public class JsonData extends AbstractData<JsonObject> {

    private static final Gson GSON = new GsonBuilder().create();
    private JsonObject json;

    @Override
    public JsonObject get() {
        return json;
    }

    @Override
    public void set(Object jsonObject) {
        if (jsonObject instanceof JsonObject) {
            this.json = (JsonObject) jsonObject;
            return;
        }
        if (jsonObject instanceof String) {
            String string = (String) jsonObject;
            this.json = GSON.fromJson(string, JsonObject.class);
            return;
        }
        throw new UnsupportedOperationException(jsonObject.getClass().getSimpleName() + " cannot be case to JsonObject");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.LONGTEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + GSON.toJson(json) + (sql ? "\"" : "");
    }
}
