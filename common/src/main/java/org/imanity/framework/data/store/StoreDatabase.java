package org.imanity.framework.data.store;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.AbstractData;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.type.DataFieldConvert;

import java.util.List;
import java.util.UUID;

public interface StoreDatabase {

    String getName();

    default String getMetadataTag() {
        return ImanityCommon.METADATA_PREFIX + this.getName();
    }

    void init(String name, Class<? extends AbstractData> data, StoreType type);

    List<DataFieldConvert> getFieldConverters();

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
