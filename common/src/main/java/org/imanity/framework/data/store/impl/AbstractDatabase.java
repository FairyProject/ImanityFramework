package org.imanity.framework.data.store.impl;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.DataHandler;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;
import org.imanity.framework.metadata.CommonMetadataRegistries;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.util.Utility;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

@Getter
public abstract class AbstractDatabase implements StoreDatabase {

    private final Map<UUID, AbstractData> dataList = new HashMap<>();

    private MetadataKey<PlayerData> metadataKey;

    protected Class<? extends AbstractData> dataClass;
    private String name;
    @Setter
    private boolean autoLoad, autoSave;
    private StoreType type;

    @Override
    public void init(String name, Class<? extends AbstractData> data, StoreType type) {
        this.name = name;
        this.dataClass = data;
        this.type = type;

        if (this.type == StoreType.PLAYER) {
            this.metadataKey = MetadataKey.create(ImanityCommon.METADATA_PREFIX + this.getName(), PlayerData.class);
        }

        this.init(name);
        DataHandler.add(data, this);
    }

    @Override
    public AbstractData load(Object object) {
        AbstractData playerData;

        UUID uuid = null;

        if (this.getType() == StoreType.PLAYER) {
            uuid = PlayerData.PLAYER_BRIDGE.getUUID(object);
        } else if (object instanceof UUID) {
            uuid = (UUID) object;
        } else {
            throw new UnsupportedOperationException("load() passed in something wrong");
        }

        Document document = this.load(uuid);
        playerData = DataHandler.fromDocument(document, this.dataClass);

        if (playerData == null) {
            playerData = this.constructData(object);
        }

        this.dataList.put(uuid, playerData);
        return playerData;
    }

    private AbstractData constructData(Object player) {
        lookup: try {

            UUID uuid = null;
            Constructor<? extends AbstractData> constructor;

            if (this.getType() == StoreType.PLAYER) {
                constructor = Utility.getConstructor(dataClass, PlayerData.PLAYER_BRIDGE.getPlayerClass());

                uuid = PlayerData.PLAYER_BRIDGE.getUUID(player);

                if (constructor != null) {
                    return constructor.newInstance(player);
                }

                constructor = Utility.getConstructor(dataClass, Object.class);

                if (constructor != null) {
                    return constructor.newInstance(player);
                }

                constructor = Utility.getConstructor(dataClass, UUID.class, String.class);

                if (constructor != null) {
                    return constructor.newInstance(
                            uuid,
                            PlayerData.PLAYER_BRIDGE.getName(player)
                    );
                }
            }

            constructor = Utility.getConstructor(dataClass, UUID.class);

            if (constructor != null) {

                if (this.getType() == StoreType.OBJECT) {
                    uuid = (UUID) player;
                    return constructor.newInstance(uuid);
                } else {
                    return constructor.newInstance(PlayerData.PLAYER_BRIDGE.getUUID(player));
                }
            }

            throw new UnsupportedOperationException(dataClass.getSimpleName() + " doesn't required constructor!");

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while loading player data", ex);
        }
    }

    public PlayerData getByPlayer(Object player) {
        if (this.type != StoreType.PLAYER) {
            throw new UnsupportedOperationException("The data type is not player but trying to get data by type");
        }
        return CommonMetadataRegistries
                .provide(PlayerData.PLAYER_BRIDGE.getUUID(player))
                .getOrNull(this.metadataKey);
    }

    public AbstractData getByUuid(UUID uuid) {
        return this.dataList.getOrDefault(uuid, null);
    }

    @Override
    public void delete(UUID uuid) {
        this.dataList.remove(uuid);

        CommonMetadataRegistries
                .get(uuid)
                .ifPresent(map -> map.remove(this.metadataKey));
    }

    public void saveAndClear() {
        for (AbstractData data : this.dataList.values()) {
            this.save(data);
        }
        this.dataList.clear();
    }

    @Nullable
    public abstract Document load(UUID uuid);

    public abstract void init(String name);

    @Override
    public boolean autoLoad() {
        return autoLoad;
    }

    @Override
    public boolean autoSave() {
        return autoSave;
    }

    @Override
    public MetadataKey<PlayerData> getMetadataTag() {
        return this.metadataKey;
    }
}
