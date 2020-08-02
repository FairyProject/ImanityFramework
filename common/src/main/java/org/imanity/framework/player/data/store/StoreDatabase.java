package org.imanity.framework.player.data.store;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.player.data.PlayerData;

public interface StoreDatabase {

    String getName();

    default String getMetadataTag() {
        return ImanityCommon.METADATA_PREFIX + this.getName();
    }

    void init(String name, Class<? extends PlayerData> data);

    PlayerData load(Object player);

    void save(PlayerData playerData);

    boolean autoLoad();

    boolean autoSave();

    void setAutoLoad(boolean bol);

    void setAutoSave(boolean bol);

    PlayerData getByPlayer(Object player);

}
