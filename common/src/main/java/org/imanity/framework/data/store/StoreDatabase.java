package org.imanity.framework.data.store;

import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.metadata.MetadataKey;

import java.util.UUID;

public interface StoreDatabase {

    String getName();

    MetadataKey<PlayerData> getMetadataTag();

    void init(String name, Class<? extends AbstractData> data, StoreType type);

    AbstractData load(Object object);

    void save(AbstractData playerData);

    void delete(UUID uuid);

    boolean autoLoad();

    boolean autoSave();

    void setAutoLoad(boolean bol);

    void setAutoSave(boolean bol);

    PlayerData getByPlayer(Object player);

    AbstractData getByUuid(UUID uuid);

    void saveAndClear();

    StoreType getType();

    Class<? extends AbstractData> getDataClass();

}
