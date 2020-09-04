package org.imanity.framework.bukkit.visual;

import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.visual.type.VisualType;

public class VisualBlock {
    private final VisualType visualType;
    private final VisualBlockData blockData;
    private final BlockPosition location;

    public VisualBlock(VisualType visualType, VisualBlockData blockData, BlockPosition location) {
        this.visualType = visualType;
        this.blockData = blockData;
        this.location = location;
    }

    public VisualType getVisualType() {
        return this.visualType;
    }

    public VisualBlockData getBlockData() {
        return this.blockData;
    }

    public BlockPosition getLocation() {
        return this.location;
    }
}
