package org.imanity.framework.bukkit.player;

import org.bukkit.entity.Player;
import org.imanity.framework.player.PlayerInfo;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.bukkit.player.type.CustomLocationDataConverter;
import org.imanity.framework.bukkit.util.CustomLocation;

public class BukkitPlayerData {

    public static void init() {
        DataConverterType.register(CustomLocationDataConverter.class, CustomLocation.class);
    }

    public static PlayerInfo toPlayerInfo(Player player) {
        return new PlayerInfo(player.getUniqueId(), player.getName());
    }

}
