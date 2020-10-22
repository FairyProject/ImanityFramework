/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.data.store.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.redisson.api.RReadWriteLock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MongoDatabase extends AbstractDatabase {

    private MongoCollection<Document> players;

    @Override
    public void init(String name) {
        this.players = ImanityCommon.MONGO.getDatabase().getCollection(ImanityCommon.MONGO.getConfig().COLLECTION_PREFIX + name);
    }

    @Override
    public Document load(UUID uuid) {
        RReadWriteLock lock = null;
        if (ImanityCommon.CORE_CONFIG.USE_REDIS_DISTRIBUTED_LOCK) {
            lock = ImanityCommon.REDIS.getLock("Data-" + uuid);
            lock.readLock().lock(3L, TimeUnit.SECONDS);
        }

        try {
            return this.players.find(Filters.eq("uuid", uuid.toString())).first();
        } finally {
            if (lock != null) {
                lock.readLock().unlock();
            }
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
                data.toDocument(),
                new ReplaceOptions().upsert(true));

        if (lock != null) {
            lock.writeLock().unlock();
        }
    }
}
