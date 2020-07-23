package org.imanity.framework.player.data;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.Imanity;
import org.imanity.framework.player.PlayerInfo;
import org.imanity.framework.player.data.annotation.StoreData;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Field;
import java.util.*;

public class PlayerData extends PlayerInfo {

    protected static final Map<Class<? extends PlayerData>, StoreDatabase> DATABASES = new HashMap<>();

    public PlayerData(Player player) {
        super(player);
    }

    public PlayerData(UUID uuid, String name) {
        super(uuid, name);
    }

    public PlayerData(UUID uuid) {
        super(uuid, "");
    }

    public void save() {
        StoreDatabase database = DATABASES.get(this.getClass());

        database.save(this);
    }

    public List<Data<?>> toDataList() {
        List<Data<?>> dataList = new ArrayList<>();

        PlayerData.getDataTypes(this.getClass())
                .forEach((key, type) -> {
                    try {
                        Field field = this.getClass().getDeclaredField(key);
                        field.setAccessible(true);

                        Data<?> data = type.newData();
                        data.setName(key);

                        data.set(field.get(this));

                        dataList.add(data);
                    } catch (Exception ex) {
                        throw new RuntimeException("Unexpected error on getting field", ex);
                    }
                });

        return dataList;
    }

    public void loadFromDocument(Document document) {
        PlayerData.getDataTypes(this.getClass()).forEach((key, type) -> {
            Data<?> data = type.newData();

            if (document.containsKey(key)) {
                try {
                    data.set(document.get(key, data.getType()));

                    Field field = this.getClass().getDeclaredField(key);
                    field.setAccessible(true);

                    field.set(this, data.toFieldObject(field));
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    throw new RuntimeException("Unexpected error while reading json files", ex);
                }
            }
        });
    }

    public static EntryArrayList<String, DataType> getDataTypes(Class<? extends PlayerData> playerDataClass) {
        EntryArrayList<String, DataType> types = new EntryArrayList<>();

        for (Field field : playerDataClass.getDeclaredFields()) {

            field.setAccessible(true);
            if (field.getAnnotation(StoreData.class) == null) {
                continue;
            }

            DataType dataType = DataType.getType(field.getType());

            if (dataType == null) {
                Imanity.LOGGER.error("The data type " + field.getType().getSimpleName() + " does not exists!");
                continue;
            }

            types.add(field.getName(), dataType);

        }

        return types;
    }

    public static <T extends PlayerData> T getPlayerData(Player player, Class<T> dataClass) {
        StoreDatabase database = PlayerData.getDatabase(dataClass);
        return (T) database.getByPlayer(player);
    }

    public static StoreDatabase getDatabase(Class<? extends PlayerData> dataClass) {
        if (DATABASES.containsKey(dataClass)) {
            return DATABASES.get(dataClass);
        }
        throw new IllegalStateException("PlayerData " + dataClass.getSimpleName() + " has not register yet!");
    }

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (StoreDatabase database : PlayerData.getStoreDatabases()) {
                PlayerData playerData = database.getByPlayer(player);
                database.save(playerData);
            }
        }
    }

    public static Collection<StoreDatabase> getStoreDatabases() {
        return PlayerData.DATABASES.values();
    }

    public static PlayerDataBuilder builder() {
        return new PlayerDataBuilder();
    }

}
