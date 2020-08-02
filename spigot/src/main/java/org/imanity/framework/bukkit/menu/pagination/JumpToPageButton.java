package org.imanity.framework.bukkit.menu.pagination;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

@AllArgsConstructor
public class JumpToPageButton extends org.imanity.framework.bukkit.menu.Button {

	private final int page;
	private final PaginatedMenu menu;
	private final boolean current;

	@Override
	public ItemStack getButtonItem(final Player player) {
		final ItemStack itemStack = new ItemStack(this.current ? Material.ENCHANTED_BOOK : Material.BOOK, this.page);
		final ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(ChatColor.YELLOW + "第" + page + "頁");

		if (this.current) {
			itemMeta.setLore(Arrays.asList(
					"",
					ChatColor.GREEN + "目前所在的頁面"
			));
		}

		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	@Override
	public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
		this.menu.modPage(player, this.page - this.menu.getPage());
		org.imanity.framework.bukkit.menu.Button.playNeutral(player);
	}

}
