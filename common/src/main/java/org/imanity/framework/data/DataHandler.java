package org.imanity.framework.data;

import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;
import org.imanity.framework.metadata.CommonMetadataRegistries;
import org.imanity.framework.metadata.MetadataMap;

import java.util.*;
import java.util.stream.Collectors;

public class DataHandler {

    protected static final Map<Class<? extends AbstractData>, StoreDatabase> DATABASES = new HashMap<>();

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
