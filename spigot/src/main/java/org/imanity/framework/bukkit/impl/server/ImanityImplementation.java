package org.imanity.framework.bukkit.impl.server;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.HologramSingle;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImanityImplementation extends NormalImplementation {
    @Override
    public List<Player> getPlayerRadius(Location location, double radius) {
        return ((CraftWorld) location.getWorld()).getHandle().playerMap
                .getNearbyPlayersIgnoreHeight(location.getX(), location.getY(), location.getZ(), 32)
                .stream().map(EntityPlayer::getBukkitEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Entity getEntity(UUID uuid) {
        return MinecraftServer
                .getServer()
                .a(uuid)
                .getBukkitEntity();
    }

    @Override
    public float getBlockSlipperiness(Material material) {
        return Block.getById(material.getId()).frictionFactor;
    }

    @Override
    public void sendHologramSpawnPacket(Player player, HologramSingle hologramSingle) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();

        packet.setA(hologramSingle.getSkullId());
        packet.setB(30);
        packet.setC((int) Math.floor(hologramSingle.getLocation().getX() * 32.0));
        packet.setD((int) Math.floor((hologramSingle.getLocation().getY() - 2.25) * 32.0));
        packet.setE((int) Math.floor(hologramSingle.getLocation().getZ() * 32.0));

        DataWatcher dataWatcher = new DataWatcher((net.minecraft.server.v1_8_R3.Entity) null);
        dataWatcher.a(0, (byte) 32);
        dataWatcher.a(2, hologramSingle.getViewHandler().view(player));
        dataWatcher.a(3, (byte) 1);

        packet.setL(dataWatcher);

        MinecraftReflection.sendPacket(player, packet);
    }

    @Override
    public void sendHologramNamePacket(Player player, HologramSingle hologramSingle) {

        DataWatcher dataWatcher = new DataWatcher((net.minecraft.server.v1_8_R3.Entity) null);
        dataWatcher.a(0, (byte) 32);
        dataWatcher.a(2, hologramSingle.getViewHandler().view(player));
        dataWatcher.a(3, (byte) 1);

        MinecraftReflection.sendPacket(player, new PacketPlayOutEntityMetadata(hologramSingle.getSkullId(), dataWatcher, true));

    }
}
