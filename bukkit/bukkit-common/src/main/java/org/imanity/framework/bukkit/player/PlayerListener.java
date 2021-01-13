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

package org.imanity.framework.bukkit.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.Component;

@Component
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinScoreboard(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        if (Imanity.BOARD_HANDLER != null) {
            Imanity.BOARD_HANDLER.getOrCreateScoreboard(player);
        }

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.registerPlayerTablist(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        if (Imanity.BOARD_HANDLER != null) {
            Imanity.BOARD_HANDLER.remove(player);
        }

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.removePlayerTablist(player);
        }

        Events.unregisterAll(player);
        Metadata.provideForPlayer(player.getUniqueId())
                .clear();
    }

}
