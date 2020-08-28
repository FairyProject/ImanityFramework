package org.imanity.framework.bukkit.visual;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.imanity.framework.bukkit.visual.type.VisualType;

@Getter
public class VisualPosition extends BlockPosition {

    private VisualType type;

    public VisualPosition(int x, int y, int z, VisualType type) {
        super(x, y ,z);
        this.type = type;
    }
}
