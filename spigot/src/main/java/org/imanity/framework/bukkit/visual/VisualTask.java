package org.imanity.framework.bukkit.visual;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.visual.type.VisualType;

import java.util.List;

@Getter
public class VisualTask {
    private final Player player;
    private final List<VisualPosition> blockPositions;

    public VisualTask(final Player player, final List<VisualPosition> blockPositions) {
        this.player = player;
        this.blockPositions = blockPositions;
    }

}
