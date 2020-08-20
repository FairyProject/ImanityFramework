package org.imanity.framework.data.store.impl;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.DataHandler;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.store.StoreType;
import org.imanity.framework.data.type.DataConverter;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.data.type.DataFieldConvert;
import org.imanity.framework.util.CommonUtility;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Constructor;
import java.util.*;

@Getter
public abstract class AbstractDatabase implements StoreDatabase {

    private final Map<UUID, AbstractData> dataList = new HashMap<>();

    protected Class<? extends AbstractData> dataClass;
    protected List<DataFieldConvert> dataConverterTypes;
    private String name;
    @Setter
    private boolean autoLoad, autoSave;
    private StoreType type;

    @Override
    public void init(String name, Class<? extends AbstractData> data, StoreType type) {
        this.name = name;
        this.dataClass = data;
        this.type = type;
        this.dataConverterTypes = AbstractData.getDataTypes(data);
        this.init(name);
        DataHandler.add(data, this);
    }

    @Override
    public List<DataFieldConvert> getFieldConverters() {
        return this.dataConverterTypes;
    }

    public Document toDocument(AbstractData data) {
        Document document = new Document();

        for (DataConverter<?> converter : data.toDataList(this)) {
            document.put(converter.name(), converter.get());
        }

        return document;
    }

    @Override
    public AbstractData load(Object player) {
        AbstractData playerData;

        UUID uuid = null;

        lookup: try {

            Constructor<? extends AbstractData> constructor;

            if (this.getType() == StoreType.PLAYER) {
                constructor = CommonUtility.getConstructor(dataClass, PlayerData.PLAYER_BRIDGE.getPlayerClass());

                uuid = PlayerData.PLAYER_BRIDGE.getUUID(player);

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
                            uuid,
                            PlayerData.PLAYER_BRIDGE.getName(player)
                    );
                    break lookup;
                }
            }

            constructor = CommonUtility.getConstructor(dataClass, UUID.class);

            if (constructor != null) {

                if (this.getType() == StoreType.OBJECT) {
                    uuid = (UUID) player;
                    playerData = constructor.newInstance(uuid);
                } else {
                    playerData = constructor.newInstance(PlayerData.PLAYER_BRIDGE.getUUID(player));
                }
                break lookup;
            }

            throw new UnsupportedOperationException(dataClass.getSimpleName() + " doesn't required constructor!");

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while loading player data", ex);
        }

        this.load(playerData);

        this.dataList.put(uuid, playerData);
        return playerData;
    }

    public PlayerData getByPlayer(Object player) {
        if (this.type != StoreType.PLAYER) {
            throw new UnsupportedOperationException("The data type is not player but trying to get data by type");
        }
        return (PlayerData) PlayerData.PLAYER_BRIDGE.getPlayerData(player, this);
    }

    public AbstractData getByUuid(UUID uuid) {
        return this.dataList.getOrDefault(uuid, null);
    }

    @Override
    public void delete(UUID uuid) {
        this.dataList.remove(uuid);
    }

    public void saveAndClear() {
        for (AbstractData data : this.dataList.values()) {
            this.save(data);
        }
        this.dataList.clear();
    }

    public abstract void load(AbstractData playerData);

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
