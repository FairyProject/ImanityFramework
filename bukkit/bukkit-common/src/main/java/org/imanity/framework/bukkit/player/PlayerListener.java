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

package org.imanity.framework.bukkit.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.player.PlayerPostJoinEvent;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.Component;
import org.imanity.framework.bukkit.timer.TimerList;
import org.imanity.framework.bukkit.timer.impl.PlayerTimer;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.metadata.MetadataMap;

@Component
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinScoreboard(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        if (Imanity.BOARD_SERVICE != null) {
            Imanity.BOARD_SERVICE.getOrCreateScoreboard(player);
        }

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.registerPlayerTablist(player);
        }

        TaskUtil.runScheduled(() -> Imanity.callEvent(new PlayerPostJoinEvent(player)), 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        if (Imanity.BOARD_SERVICE != null) {
            Imanity.BOARD_SERVICE.remove(player);
        }

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.removePlayerTablist(player);
        }

        Events.unregisterAll(player);
        MetadataMap metadataMap = Metadata.provideForPlayer(player);
        metadataMap.ifPresent(PlayerTimer.TIMER_METADATA_KEY, TimerList::clear);
        for (MetadataKey<?> key : metadataMap.asMap().keySet()) {
            if (key.removeOnNonExists()) {
                metadataMap.remove(key);
            }
        }
    }

}
