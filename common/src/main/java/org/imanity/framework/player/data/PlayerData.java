package org.imanity.framework.player.data;

import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.player.PlayerBridge;
import org.imanity.framework.player.PlayerInfo;
import org.imanity.framework.player.data.annotation.StoreData;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class PlayerData extends PlayerInfo {

    public static PlayerBridge PLAYER_BRIDGE;
    protected static final Map<Class<? extends PlayerData>, StoreDatabase> DATABASES = new HashMap<>();

    public PlayerData(Object player) {
        this(PlayerData.PLAYER_BRIDGE.getUUID(player),
                PlayerData.PLAYER_BRIDGE.getName(player));
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

    public Document toDocument() {
        Document document = new Document();

        document.put("uuid", this.getUuid().toString());
        document.put("name", this.getName());

        for (Data<?> data : this.toDataList()) {
            document.put(data.name(), data.get());
        }

        return document;
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
                    data.set(document.get(key, data.getLoadType()));

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

            if (Modifier.isFinal(field.getModifiers())) {
                new IllegalStateException("The field " + field.getName() + " is final but it's storing datas!").printStackTrace();
                continue;
            }

            if (Modifier.isStatic(field.getModifiers())) {
                new IllegalStateException("The field " + field.getName() + " is static but it's storing datas!").printStackTrace();
                continue;
            }

            DataType dataType = DataType.getType(field);

            if (dataType == null) {
                ImanityCommon.BRIDGE.getLogger().error("The data type " + field.getType().getSimpleName() + " does not exists!");
                continue;
            }

            types.add(field.getName(), dataType);

        }

        return types;
    }

    public static StoreDatabase getDatabase(Class<? extends PlayerData> dataClass) {
        if (DATABASES.containsKey(dataClass)) {
            return DATABASES.get(dataClass);
        }
        throw new IllegalStateException("PlayerData " + dataClass.getSimpleName() + " has not register yet!");
    }

    public static <T extends PlayerData> T getPlayerData(Object player, Class<T> dataClass) {
        StoreDatabase database = PlayerData.getDatabase(dataClass);
        return (T) database.getByPlayer(player);
    }

    public static void shutdown() {
        for (Object player : PLAYER_BRIDGE.getOnlinePlayers()) {
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
