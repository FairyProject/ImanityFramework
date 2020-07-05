package org.imanity.framework.player.data.store.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.imanity.framework.Imanity;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;

import java.lang.reflect.Field;
import java.util.Map;

public class MongoDatabase extends AbstractDatabase {

    private MongoCollection<Document> players;

    @Override
    public void init(String name) {
        this.players = Imanity.MONGO.getDatabase().getCollection(Imanity.MONGO.getConfig().COLLECTION_PREFIX + name);
    }

    @Override
    public void load(PlayerData playerData) {
        Document document = this.players.find(Filters.eq("uuid", playerData.getUuid().toString())).first();

        if (document == null) {
            return;
        }

        try {
            for (Map.Entry<String, DataType> entry : this.getDataTypes().entrySet()) {
                Data<?> data = entry.getValue().newData();

                if (document.containsKey(entry.getKey())) {
                    data.set(document.get(entry.getKey(), data.getType()));

                    Field field = playerData.getClass().getDeclaredField(entry.getKey());
                    field.setAccessible(true);
                    field.set(playerData, data.toFieldObject(field));
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException("Unexpected error while loading PlayerData", ex);
        }
    }

    @Override
    public void save(PlayerData playerData) {
        Document document = new Document();

        document.put("uuid", playerData.getUuid().toString());
        document.put("name", playerData.getName());

        for (Data<?> data : playerData.toDataList()) {
            document.put(data.name(), data.get());
        }

        this.players.replaceOne(Filters.eq("uuid", playerData.getUuid().toString()), document, new ReplaceOptions().upsert(true));
    }
}
