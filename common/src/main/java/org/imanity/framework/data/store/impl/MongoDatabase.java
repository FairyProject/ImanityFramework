package org.imanity.framework.data.store.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.redisson.api.RReadWriteLock;

import java.util.concurrent.TimeUnit;

public class MongoDatabase extends AbstractDatabase {

    private MongoCollection<Document> players;

    @Override
    public void init(String name) {
        this.players = ImanityCommon.MONGO.getDatabase().getCollection(ImanityCommon.MONGO.getConfig().COLLECTION_PREFIX + name);
    }

    @Override
    public void load(AbstractData data) {
        RReadWriteLock lock = null;
        if (ImanityCommon.CORE_CONFIG.USE_REDIS_DISTRIBUTED_LOCK) {
            lock = ImanityCommon.REDIS.getLock("Data-" + data.getUuid());
            lock.readLock().lock(3L, TimeUnit.SECONDS);
        }

        Document document = this.players.find(Filters.eq("uuid", data.getUuid().toString())).first();

        if (document == null) {
            return;
        }

        data.loadFromDocument(document, this);

        if (lock != null) {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(AbstractData data) {
        RReadWriteLock lock = null;
        if (ImanityCommon.CORE_CONFIG.USE_REDIS_DISTRIBUTED_LOCK) {
            lock = ImanityCommon.REDIS.getLock("Data-" + data.getUuid());
            lock.writeLock().lock(3L, TimeUnit.SECONDS);
        }

        this.players.replaceOne(Filters.eq("uuid", data.getUuid().toString()),
                this.toDocument(data),
                new ReplaceOptions().upsert(true));

        if (lock != null) {
            lock.writeLock().unlock();
        }
    }
}
