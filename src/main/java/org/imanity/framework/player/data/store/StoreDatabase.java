package org.imanity.framework.player.data.store;

import org.bukkit.entity.Player;
import org.imanity.framework.Imanity;
import org.imanity.framework.player.data.PlayerData;

public interface StoreDatabase {

    String getName();

    default String getMetadataTag() {
        return Imanity.METADATA_PREFIX + this.getName();
    }

    void init(String name, Class<? extends PlayerData> data);

    PlayerData load(Player player);

    void save(PlayerData playerData);

    boolean autoLoad();

    boolean autoSave();

    void setAutoLoad(boolean bol);

    void setAutoSave(boolean bol);

    PlayerData getByPlayer(Player player);

}
