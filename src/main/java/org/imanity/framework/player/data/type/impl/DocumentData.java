package org.imanity.framework.player.data.type.impl;

import lombok.NoArgsConstructor;
import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.bson.Document;

@NoArgsConstructor
public class DocumentData extends AbstactData<Document> {

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
            this.document = Document.parse(string);
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
        return (sql ? "\"" : "") + document.toJson() + (sql ? "\"" : "");
    }
}
