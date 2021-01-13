/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.menu;

import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.menu.pagination.PaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.imanity.framework.Component;

@Component
public class ButtonListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onButtonPress(final InventoryClickEvent event) {

		final Player player = (Player) event.getWhoClicked();

		final Menu openMenu = Menu.MENUS.get(player.getUniqueId());

		if (openMenu != null) {

			if (event.getSlot() != event.getRawSlot()) {
				if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);
				}
				return;
			}

			if (openMenu.getButtons().containsKey(event.getSlot())) {

				final org.imanity.framework.bukkit.menu.Button button = openMenu.getButtons().get(event.getSlot());
				final boolean cancel = button.shouldCancel(player, event.getSlot(), event.getClick());

				if (!cancel &&
						(event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);

					if (event.getCurrentItem() != null) {
						player.getInventory().addItem(event.getCurrentItem());
					}
				} else {
					event.setCancelled(cancel);
				}

				button.clicked(player, event.getSlot(), event.getClick(), event.getHotbarButton());

				openMenu.setLastAccessMillis(System.currentTimeMillis());

				if (Menu.MENUS.containsKey(player.getUniqueId())) {
					final Menu newMenu = Menu.MENUS.get(player.getUniqueId());

					if (newMenu == openMenu) {
						final boolean buttonUpdate = button.shouldUpdate(player, event.getSlot(), event.getClick());

						if ((newMenu.isUpdateAfterClick() && buttonUpdate) || buttonUpdate) {
							openMenu.setClosedByMenu(true);
							newMenu.openMenu(player);
						}
					}
				} else if (button.shouldUpdate(player, event.getSlot(), event.getClick())) {
					openMenu.setClosedByMenu(true);
					openMenu.openMenu(player);
				}

				if (event.isCancelled()) {
					Bukkit.getScheduler().runTaskLater(Imanity.PLUGIN, player::updateInventory, 1L);
				}
			} else {
				if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		final Menu openMenu = Menu.MENUS.get(player.getUniqueId());

		if (openMenu != null) {
			openMenu.onClose(player);

			Menu.MENUS.remove(player.getUniqueId());

			if (openMenu instanceof PaginatedMenu)
				return;
		}

	}

}