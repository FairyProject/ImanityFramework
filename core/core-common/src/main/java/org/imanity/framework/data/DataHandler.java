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

package org.imanity.framework.data;

import org.bson.Document;
import org.imanity.framework.data.mapping.DataElementMapper;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;
import org.imanity.framework.metadata.CommonMetadataRegistries;
import org.imanity.framework.metadata.MetadataMap;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DataHandler {

    protected static final Map<Class<? extends AbstractData>, StoreDatabase> DATABASES = new HashMap<>();
    public static final DataElementMapper MAPPER = new DataElementMapper();

    public static <T extends AbstractData> T fromDocument(@Nullable Document document, Class<T> type) {
        if (document == null) {
            return null;
        }
        return MAPPER.fromJson(document, type);
    }

    public static void add(Class<? extends AbstractData> dataClass, StoreDatabase database) {
        DATABASES.put(dataClass, database);
    }

    public static StoreDatabase getDatabase(Class<? extends AbstractData> dataClass) {
        return DATABASES.get(dataClass);
    }

    public static <T extends PlayerData> boolean hasData(Object player, Class<T> dataClass) {
        return hasData(player, DataHandler.getDatabase(dataClass));
    }

    public static boolean hasData(Object player, StoreDatabase database) {
        MetadataMap map = CommonMetadataRegistries.getOrNull(PlayerData.PLAYER_BRIDGE.getUUID(player));

        if (map == null) {
            return false;
        }

        return map.has(database.getMetadataTag());
    }


    public static <T extends PlayerData> T getPlayerData(Object player, Class<T> dataClass) {
        StoreDatabase database = DataHandler.getDatabase(dataClass);

        if (!DataHandler.hasData(player, database)) {
            return null;
        }

        return (T) database.getByPlayer(player);
    }

    public static <T extends AbstractData> T load(UUID uuid, Class<T> dataClass) {
        StoreDatabase database = DataHandler.getDatabase(dataClass);
        return (T) database.load(uuid);
    }

    public static <T extends AbstractData> void save(UUID uuid, Class<T> dataClass) {
        T data = DataHandler.getData(uuid, dataClass);
        data.save();
    }

    public static <T extends AbstractData> void delete(T data) {
        StoreDatabase database = DataHandler.getDatabase(data.getClass());
        database.delete(data.getUuid());
    }

    public static <T extends AbstractData> T getData(UUID uuid, Class<T> dataClass) {
        StoreDatabase database = DataHandler.getDatabase(dataClass);
        return (T) database.getByUuid(uuid);
    }

    public static Collection<StoreDatabase> getStoreDatabases() {
        return DATABASES.values();
    }

    public static List<StoreDatabase> getPlayerDatabases() {
        return DataHandler
                .getStoreDatabases()
                .stream()
                .filter(database -> database.getType() == StoreType.PLAYER)
                .collect(Collectors.toList());
    }

    public static void shutdown() {
        for (StoreDatabase database : DATABASES.values()) {
            database.saveAndClear();
        }
    }
}
