package org.imanity.framework.bukkit.visual.type;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.visual.VisualBlockData;

public abstract class VisualType {
	@Deprecated
	VisualBlockData generate(final Player player, final int x, final int y, final int z) {
		return generate(player, new BlockPosition(x, y, z, player.getWorld().getName()));
	}

	public abstract VisualBlockData generate(Player paramPlayer, BlockPosition paramLocation);
}
