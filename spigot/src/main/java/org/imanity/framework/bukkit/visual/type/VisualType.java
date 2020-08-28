package org.imanity.framework.bukkit.visual.type;

import com.google.common.collect.Iterables;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.visual.VisualBlockData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class VisualType {
	@Deprecated
	VisualBlockData generate(final Player player, final int x, final int y, final int z) {
		return generate(player, new BlockPosition(x, y, z));
	}

	public abstract VisualBlockData generate(Player paramPlayer, BlockPosition paramLocation);
}
