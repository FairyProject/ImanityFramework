package org.imanity.framework.player.data.store.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.player.data.PlayerData;

public class MongoDatabase extends AbstractDatabase {

    private MongoCollection<Document> players;

    @Override
    public void init(String name) {
        this.players = ImanityCommon.MONGO.getDatabase().getCollection(ImanityCommon.MONGO.getConfig().COLLECTION_PREFIX + name);
    }

    @Override
    public void load(PlayerData playerData) {
        Document document = this.players.find(Filters.eq("uuid", playerData.getUuid().toString())).first();

        if (document == null) {
            return;
        }

        playerData.loadFromDocument(document);
    }

    @Override
    public void save(PlayerData playerData) {
        this.players.replaceOne(Filters.eq("uuid", playerData.getUuid().toString()),
                playerData.toDocument(),
                new ReplaceOptions().upsert(true));
    }
}
