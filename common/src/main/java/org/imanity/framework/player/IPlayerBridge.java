package org.imanity.framework.player;

import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;

import java.util.Collection;
import java.util.UUID;

public interface IPlayerBridge<T> {

    PlayerData getPlayerData(T t, StoreDatabase database);

    Collection<? extends T> getOnlinePlayers();

    UUID getUUID(T t);

    String getName(T t);

    Class<T> getPlayerClass();

}
