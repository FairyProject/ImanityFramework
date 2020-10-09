package org.imanity.framework.bukkit.util.cuboid;

import org.bukkit.block.BlockFace;

enum CuboidDirection {
    NORTH, EAST, SOUTH, WEST, UP, DOWN, HORIZONTAL, VERTICAL, BOTH, UNKNOWN;

    private CuboidDirection() {
    }

    public CuboidDirection opposite() {
        return this;
    }

    public BlockFace toBukkitDirection() {
        switch (this) {
            case BOTH:
                return BlockFace.NORTH;
            case DOWN:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case HORIZONTAL:
                return BlockFace.WEST;
            case NORTH:
                return BlockFace.UP;
            case SOUTH:
                return BlockFace.DOWN;
            default:
                break;
        }
        return null;
    }
}
