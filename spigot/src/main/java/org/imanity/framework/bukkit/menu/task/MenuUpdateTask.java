package org.imanity.framework.bukkit.menu.task;

import org.imanity.framework.bukkit.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.TaskUtil;

import java.util.concurrent.TimeUnit;

public class MenuUpdateTask implements Runnable {

	private static final long AUTO_CLOSE_MILLIS = TimeUnit.SECONDS.toMillis(30L);

	public static void init() {
		TaskUtil.runRepeated(new MenuUpdateTask(), 20 * 5L);
	}

	@Override
	public void run() {
		Menu.MENUS.forEach((uuid, menu) -> {
			final Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}
			if (menu.isAutoUpdate()) {
				menu.openMenu(player, true);
			}
			long openMillis = System.currentTimeMillis() - menu.getLastAccessMillis();
			if (menu.isAutoClose() && openMillis > AUTO_CLOSE_MILLIS) {
				player.closeInventory();
			}
		});
	}

}
