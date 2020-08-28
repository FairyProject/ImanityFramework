package org.imanity.framework.bukkit.visual;

import org.bukkit.World;
import org.imanity.framework.bukkit.visual.type.VisualType;

public interface VisualBlockClaim {

    World getWorld();

    int getMinX();

    int getMaxX();

    int getMinY();

    int getMaxY();

    int getMinZ();

    int getMaxZ();

    VisualType getType();

}
