package org.imanity.framework.player.data.store.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class AbstractDatabase implements StoreDatabase {

    protected Class<? extends PlayerData> dataClass;
    protected Map<String, DataType> dataTypes;
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
    public PlayerData load(Player player) {
        PlayerData playerData;

        lookup: try {

            Constructor<? extends PlayerData> constructor;

            constructor = ReflectionUtil.getConstructor(dataClass, Player.class);

            if (constructor != null) {
                playerData = constructor.newInstance(player);
                break lookup;
            }

            constructor = ReflectionUtil.getConstructor(dataClass, UUID.class, String.class);

            if (constructor != null) {
                playerData = constructor.newInstance(player.getUniqueId(), player.getName());
                break lookup;
            }

            constructor = ReflectionUtil.getConstructor(dataClass, UUID.class);

            if (constructor != null) {
                playerData = constructor.newInstance(player.getUniqueId());
                break lookup;
            }

            throw new UnsupportedOperationException(dataClass.getSimpleName() + " doesn't required constructor!");

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while loading player data", ex);
        }

        this.load(playerData);

        return playerData;
    }

    public PlayerData getByPlayer(Player player) {
        return (PlayerData) player.getMetadata(this.getMetadataTag()).get(0).value();
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
