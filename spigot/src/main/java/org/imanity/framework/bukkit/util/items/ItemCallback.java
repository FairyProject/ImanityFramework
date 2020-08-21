package org.imanity.framework.bukkit.util.items;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ItemCallback {

    boolean onClick(Player player, ItemStack itemStack, Action action);

}
