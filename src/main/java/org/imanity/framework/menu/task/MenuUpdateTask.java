package org.imanity.framework.menu.task;

import org.imanity.framework.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.util.TaskUtil;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MenuUpdateTask implements Runnable {

	private long autoCloseMillis = TimeUnit.SECONDS.toMillis(30L);

	public static void init() {
		TaskUtil.runAsyncRepeated(new MenuUpdateTask(), 20 * 5L);
	}

	@Override
	public void run() {
		Menu.currentlyOpenedMenus.forEach((uuid, menu) -> {
			final Player player = Bukkit.getPlayer(uuid);
			if (player == null)
				return;
			if (menu.isAutoUpdate()) {
				menu.openMenu(player, true);
			}
			if (menu.isAutoClose() && System.currentTimeMillis() - menu.getLastAccessMillis() > autoCloseMillis) {
				player.closeInventory();
			}
		});
	}

}
