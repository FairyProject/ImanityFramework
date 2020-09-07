package org.imanity.framework.bukkit.util;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Objects;

@Data
public class BlockPosition {

    private String world;
    private int x;
    private int y;
    private int z;

    public BlockPosition(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public BlockPosition() {
        this(0 , 0, 0, Bukkit.getWorlds().get(0).getName());
    }

    public static BlockPosition of(Location location) {
        Objects.requireNonNull(location, "location");
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public static BlockPosition of(Block block) {
        Objects.requireNonNull(block, "block");
        return of(block.getLocation());
    }

}
