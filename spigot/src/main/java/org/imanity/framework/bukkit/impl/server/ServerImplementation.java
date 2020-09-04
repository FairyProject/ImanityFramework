package org.imanity.framework.bukkit.impl.server;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.visual.VisualPosition;

import java.util.List;
import java.util.Map;

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

    void showDyingNPC(Player player);

    Object toBlockNMS(MaterialData materialData);

    List<Player> getPlayerRadius(Location location, double radius);

    void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> positions, List<BlockPosition> toRemove, boolean send);

    void clearFakeBlocks(Player player, boolean send);

    void sendActionBar(Player player, String message);
}
