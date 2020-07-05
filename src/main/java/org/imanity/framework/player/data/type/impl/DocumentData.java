package org.imanity.framework.player.data.type.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.bson.Document;

@NoArgsConstructor
public class DocumentData extends AbstactData<Document> {

    private static final Gson GSON = new GsonBuilder().create();
    private Document document;

    @Override
    public Document get() {
        return document;
    }

    @Override
    public void set(Object jsonObject) {
        if (jsonObject instanceof Document) {
            this.document = (Document) jsonObject;
            return;
        }
        if (jsonObject instanceof String) {
            String string = (String) jsonObject;
            this.document = GSON.fromJson(string, Document.class);
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
        return (sql ? "\"" : "") + GSON.toJson(this.document) + (sql ? "\"" : "");
    }
}
