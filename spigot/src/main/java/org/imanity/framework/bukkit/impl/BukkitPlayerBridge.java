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
