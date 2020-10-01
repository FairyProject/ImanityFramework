package org.imanity.framework.bukkit.player;

import org.bukkit.entity.Player;
import org.imanity.framework.player.PlayerInfo;

public class BukkitPlayerData {

    public static PlayerInfo toPlayerInfo(Player player) {
        return new PlayerInfo(player.getUniqueId(), player.getName());
    }

}
