package org.imanity.framework.player.data;

import org.bukkit.entity.Player;
import org.imanity.framework.Imanity;
import org.imanity.framework.player.PlayerInfo;
import org.imanity.framework.player.data.annotation.StoreData;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;

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

        for (Field field : this.getClass().getDeclaredFields()) {

            field.setAccessible(true);
            if (field.getAnnotation(StoreData.class) == null) {
                continue;
            }

            DataType dataType = DataType.getType(field.getDeclaringClass());

            if (dataType == null) {
                Imanity.LOGGER.error("The data type " + field.getDeclaringClass().getSimpleName() + " does not exists!");
                continue;
            }

            Data<?> data = dataType.newData();
            data.setName(field.getName());
            try {
                data.set(field.get(this));
            } catch (Exception ex) {
                throw new RuntimeException("Unexpected error on getting field", ex);
            }

            dataList.add(data);

        }

        return dataList;
    }

    public static Map<String, DataType> getDataTypes(Class<? extends PlayerData> playerDataClass) {
        Map<String, DataType> types = new HashMap<>();

        for (Field field : playerDataClass.getDeclaredFields()) {

            field.setAccessible(true);
            if (field.getAnnotation(StoreData.class) == null) {
                continue;
            }

            DataType dataType = DataType.getType(field.getDeclaringClass());

            if (dataType == null) {
                Imanity.LOGGER.error("The data type " + field.getDeclaringClass().getSimpleName() + " does not exists!");
                continue;
            }

            types.put(field.getName(), dataType);

        }

        return types;
    }

    public static PlayerData getPlayerData(Player player, Class<? extends PlayerData> dataClass) {
        StoreDatabase database = PlayerData.getDatabase(dataClass);
        return database.getByPlayer(player);
    }

    public static StoreDatabase getDatabase(Class<? extends PlayerData> dataClass) {
        if (DATABASES.containsKey(dataClass)) {
            return DATABASES.get(dataClass);
        }
        throw new IllegalStateException("PlayerData " + dataClass.getSimpleName() + " has not register yet!");
    }

    public static Collection<StoreDatabase> getStoreDatabases() {
        return PlayerData.DATABASES.values();
    }

    public static PlayerDataBuilder builder() {
        return new PlayerDataBuilder();
    }

}
