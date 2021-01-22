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
