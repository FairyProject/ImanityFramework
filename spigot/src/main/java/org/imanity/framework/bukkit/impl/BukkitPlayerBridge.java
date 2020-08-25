package org.imanity.framework.bukkit.impl;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.player.IPlayerBridge;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;

import java.util.Collection;
import java.util.UUID;

public class BukkitPlayerBridge implements IPlayerBridge<Player> {
    @Override
    public PlayerData getPlayerData(Player player, StoreDatabase database) {
        try {
            return (PlayerData) player.getMetadata(database.getMetadataTag()).get(0).value();
        } catch (IndexOutOfBoundsException throwable) {
            throw new IllegalStateException("Metadata " + database.getMetadataTag() + " not exists!", throwable);
        }
    }

    @Override
    public boolean hasData(Player player, StoreDatabase database) {
        return player.hasMetadata(database.getMetadataTag());
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Imanity.PLUGIN.getServer().getOnlinePlayers();
    }
    @Override
    public UUID getUUID(Player player) {
        return player.getUniqueId();
    }

    @Override
    public String getName(Player player) {
        return player.getName();
    }

    @Override
    public Class<Player> getPlayerClass() {
        return Player.class;
    }
}
