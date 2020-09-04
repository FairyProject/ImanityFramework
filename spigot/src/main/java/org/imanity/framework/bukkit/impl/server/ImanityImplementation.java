package org.imanity.framework.bukkit.impl.server;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class ImanityImplementation extends NormalImplementation {
    @Override
    public List<Player> getPlayerRadius(Location location, double radius) {
        return ((CraftWorld) location.getWorld()).getHandle().playerMap
                .getNearbyPlayersIgnoreHeight(location.getX(), location.getY(), location.getZ(), 32)
                .stream().map(EntityPlayer::getBukkitEntity)
                .collect(Collectors.toList());
    }
}
