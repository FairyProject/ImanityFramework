/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.imanity.framework.Component;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.util.Stacktrace;

@Component
public class MenuListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onButtonPress(final InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		Menu menu = Menu.getMenuByUuid(player.getUniqueId());

		if (menu != null) {
			if (event.getSlot() != event.getRawSlot()) {
				if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);
				}
				return;
			}

			if (menu.getButtons().containsKey(event.getSlot())) {
				Button button = menu.getButtons().get(event.getSlot());
				boolean cancel = button.shouldCancel(player, event.getSlot(), event.getClick());

				if (!cancel && (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);

					if (event.getCurrentItem() != null) {
						player.getInventory().addItem(event.getCurrentItem());
					}
				} else {
					event.setCancelled(cancel);
				}

				button.clicked(player, event.getSlot(), event.getClick(), event.getHotbarButton());
				menu.setLastAccessMillis(System.currentTimeMillis());

				if (Menu.MENU_BY_UUID.containsKey(player.getUniqueId())) {
					final Menu newMenu = Menu.MENU_BY_UUID.get(player.getUniqueId());

					if (newMenu == menu) {
						final boolean buttonUpdate = button.shouldUpdate(player, event.getSlot(), event.getClick());

						if (newMenu.isUpdateAfterClick() || buttonUpdate) {
							menu.setClosedByMenu(true);
							newMenu.openMenu(player);
						}
					}
				} else if (button.shouldUpdate(player, event.getSlot(), event.getClick())) {
					menu.setClosedByMenu(true);
					menu.openMenu(player);
				}

				if (event.isCancelled()) {
					BukkitUtil.delayedUpdateInventory(player);
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
		final Menu openMenu = Menu.getMenuByUuid(player.getUniqueId());

		if (openMenu != null) {
			try {
				openMenu.onClose(player);
			} catch (Throwable throwable) {
				Stacktrace.print(throwable);
			}

			Menu.MENU_BY_UUID.remove(player.getUniqueId());
		}

	}

}