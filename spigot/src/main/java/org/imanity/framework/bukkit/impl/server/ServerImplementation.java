package org.imanity.framework.bukkit.impl.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.imanity.framework.bukkit.hologram.HologramSingle;
import org.imanity.framework.bukkit.nametag.NameTagInfo;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.visual.VisualPosition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServerImplementation {

    static ServerImplementation load() {
        switch (SpigotUtil.SPIGOT_TYPE) {
            case PAPER:
            case SPIGOT:
            case CRAFTBUKKIT:
                return new NormalImplementation();
            case IMANITY:
                return new ImanityImplementation();
        }
        return null;
    }

    Entity getEntity(UUID uuid);

    Entity getEntity(World world, int id);

    default Entity getEntity(int id) {
        for (World world : Bukkit.getWorlds()) {
            Entity entity = this.getEntity(world, id);
            if (entity != null) {
                return entity;
            }
        }

        return null;
    }

    void showDyingNPC(Player player);

    Object toBlockNMS(MaterialData materialData);

    List<Player> getPlayerRadius(Location location, double radius);

    void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> positions, List<BlockPosition> toRemove, boolean send);

    void clearFakeBlocks(Player player, boolean send);

    void sendActionBar(Player player, String message);

    float getBlockSlipperiness(Material material);

    void sendTeam(Player player, String name, String prefix, String suffix, Collection<String> nameSet, int type);

    void sendMember(Player player, String name, Collection<String> players, int type);

    void sendEntityDestroy(Player player, int... ids);

    void sendEntityTeleport(Player player, Location location, int id);

    void sendEntityAttach(Player player, int type, int toAttach, int attachTo);

    void sendHologramSpawnPacket(Player player, HologramSingle hologramSingle);

    void sendHologramNamePacket(Player player, HologramSingle hologramSingle);
}
