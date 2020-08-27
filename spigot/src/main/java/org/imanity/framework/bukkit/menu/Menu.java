package org.imanity.framework.bukkit.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.util.entry.Entry;
import spg.lgdev.util.triemap.TrieMap;

import java.util.*;

@Getter
@Setter
public abstract class Menu {

	public static final Map<UUID, Menu> MENUS = TrieMap.create();

	public static <T extends Menu> List<Entry<Player, T>> getMenusByType(Class<T> type) {
		List<Entry<Player, T>> menus = new ArrayList<>();

		for (Map.Entry<UUID, Menu> entry : MENUS.entrySet()) {
			if (type.isInstance(entry.getValue())) {
				Player player = Bukkit.getPlayer(entry.getKey());

				if (player == null) {
					continue;
				}

				menus.add(new Entry<>(player, (T) entry.getValue()));
			}
		}

		return menus;
	}

	@Getter
	private Map<Integer, org.imanity.framework.bukkit.menu.Button> buttons = new HashMap<>();
	private boolean autoUpdate = true;
	private boolean updateAfterClick = true;
	private boolean autoClose = false;
	private boolean closedByMenu = false;
	private boolean placeholder = false;
	private long openMillis, lastAccessMillis;
	private org.imanity.framework.bukkit.menu.Button placeholderButton = org.imanity.framework.bukkit.menu.Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");

	private ItemStack createItemStack(final Player player, final org.imanity.framework.bukkit.menu.Button button) {
		return button.getButtonItem(player);
	}

	public void openMenu(final Player player) {
		this.openMenu(player, false);
		openMillis = System.currentTimeMillis();
		lastAccessMillis = openMillis;
	}

	public void openMenu(final Player player, boolean update) {
		this.buttons = this.getButtons(player);

		final org.imanity.framework.bukkit.menu.Menu previousMenu = org.imanity.framework.bukkit.menu.Menu.MENUS.get(player.getUniqueId());
		Inventory inventory = null;
		final int size = this.getSize() == -1 ? this.size(this.buttons) : this.getSize();
		String title = this.getTitle(player);

		if (title.length() > 32) {
			title = title.substring(0, 32);
		}

		if (player.getOpenInventory() != null) {
			if (previousMenu == null) {
				player.closeInventory();
			} else {
				final int previousSize = player.getOpenInventory().getTopInventory().getSize();

				if (previousSize == size && player.getOpenInventory().getTopInventory().getTitle().equals(title)) {
					inventory = player.getOpenInventory().getTopInventory();
					update = true;
				} else {
					previousMenu.setClosedByMenu(true);
					player.closeInventory();
				}
			}
		}

		if (inventory == null) {
			inventory = Bukkit.createInventory(player, size, title);
		}

		inventory.setContents(new ItemStack[inventory.getSize()]);

		MENUS.put(player.getUniqueId(), this);

		for (final Map.Entry<Integer, org.imanity.framework.bukkit.menu.Button> buttonEntry : this.buttons.entrySet()) {
			inventory.setItem(buttonEntry.getKey(), createItemStack(player, buttonEntry.getValue()));
		}

		if (this.isPlaceholder()) {
			for (int index = 0; index < size; index++) {
				if (this.buttons.get(index) == null) {
					this.buttons.put(index, this.placeholderButton);
					inventory.setItem(index, this.placeholderButton.getButtonItem(player));
				}
			}
		}

		if (update) {
			player.updateInventory();
		} else {
			player.openInventory(inventory);
		}

		this.onOpen(player);
		this.setClosedByMenu(false);
	}

	public Map<Integer, Button> getButtonsUpdatable(Player player) {
		return this.getButtons(player);
	}

	public void update(Player player, boolean removingOld) {
		this.buttons = this.getButtonsUpdatable(player);
		int size = this.getSize() == -1 ? this.size(this.buttons) : this.getSize();
		Inventory inventory = player.getOpenInventory().getTopInventory();
		if (inventory != null) {
			if (removingOld) {
				inventory.setContents(new ItemStack[inventory.getSize()]);
			}
			Iterator var4 = this.buttons.entrySet().iterator();

			while(var4.hasNext()) {
				Map.Entry<Integer, Button> buttonEntry = (Map.Entry)var4.next();
				inventory.setItem((Integer)buttonEntry.getKey(), this.createItemStack(player, (Button)buttonEntry.getValue()));
			}

			if (this.isPlaceholder()) {
				for(int index = 0; index < size; ++index) {
					if (this.buttons.get(index) == null) {
						this.buttons.put(index, this.placeholderButton);
						inventory.setItem(index, this.placeholderButton.getButtonItem(player));
					}
				}
			}

			player.updateInventory();
		}
	}

	public int size(final Map<Integer, org.imanity.framework.bukkit.menu.Button> buttons) {
		int highest = 0;

		for (final int buttonValue : buttons.keySet()) {
			if (buttonValue > highest) {
				highest = buttonValue;
			}
		}

		return (int) (Math.ceil((highest + 1) / 9D) * 9D);
	}

	public int getSlot(final int x, final int y) {
		return ((9 * y) + x);
	}

	public int getSize() {
		return -1;
	}

	public abstract String getTitle(Player player);

	public abstract Map<Integer, org.imanity.framework.bukkit.menu.Button> getButtons(Player player);

	public void onOpen(final Player player) {
	}

	public void onClose(final Player player) {
	}

}