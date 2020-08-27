package org.imanity.framework.bukkit.zigguart.utils.version.protocol;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ProtocolCheckImanitySpigot implements ProtocolCheck {
    @Override
    public int getVersion(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion();
    }
}
