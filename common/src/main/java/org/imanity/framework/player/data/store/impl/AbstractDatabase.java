package org.imanity.framework.player.data.store.impl;

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.util.CommonUtility;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.UUID;

@Getter
public abstract class AbstractDatabase implements StoreDatabase {

    protected Class<? extends PlayerData> dataClass;
    protected EntryArrayList<String, DataType> dataTypes;
    private String name;
    @Setter
    private boolean autoLoad, autoSave;

    @Override
    public void init(String name, Class<? extends PlayerData> data) {
        this.name = name;
        this.dataClass = data;
        this.dataTypes = PlayerData.getDataTypes(data);
        this.init(name);
    }

    @Override
    public PlayerData load(Object player) {
        PlayerData playerData;


        lookup: try {

            Constructor<? extends PlayerData> constructor;

            System.out.println("hi!!!");
            System.out.println(Arrays.toString(dataClass.getConstructors()));

            constructor = CommonUtility.getConstructor(dataClass, PlayerData.PLAYER_BRIDGE.getPlayerClass());

            if (constructor != null) {
                playerData = constructor.newInstance(player);
                break lookup;
            }

            constructor = CommonUtility.getConstructor(dataClass, Object.class);

            if (constructor != null) {
                playerData = constructor.newInstance(player);
                break lookup;
            }

            constructor = CommonUtility.getConstructor(dataClass, UUID.class, String.class);

            if (constructor != null) {
                playerData = constructor.newInstance(
                        PlayerData.PLAYER_BRIDGE.getUUID(player),
                        PlayerData.PLAYER_BRIDGE.getName(player)
                );
                break lookup;
            }

            constructor = CommonUtility.getConstructor(dataClass, UUID.class);

            if (constructor != null) {
                playerData = constructor.newInstance(PlayerData.PLAYER_BRIDGE.getUUID(player));
                break lookup;
            }

            throw new UnsupportedOperationException(dataClass.getSimpleName() + " doesn't required constructor!");

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while loading player data", ex);
        }

        this.load(playerData);

        return playerData;
    }

    public PlayerData getByPlayer(Object player) {
        return PlayerData.PLAYER_BRIDGE.getPlayerData(player, this);
    }

    public abstract void load(PlayerData playerData);

    public abstract void init(String name);

    @Override
    public boolean autoLoad() {
        return autoLoad;
    }

    @Override
    public boolean autoSave() {
        return autoSave;
    }
}
