package org.imanity.framework.bukkit.menu.buttons;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@AllArgsConstructor
public class BackButton extends org.imanity.framework.bukkit.menu.Button {

	private final org.imanity.framework.bukkit.menu.Menu back;

	@Override
	public ItemStack getButtonItem(final Player player) {
		final ItemStack itemStack = new ItemStack(Material.BED);
		final ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(ChatColor.RED + "返回");
		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	@Override
	public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
		org.imanity.framework.bukkit.menu.Button.playNeutral(player);

		this.back.openMenu(player);
	}

}
