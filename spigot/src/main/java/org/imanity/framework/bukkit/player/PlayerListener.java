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
import org.imanity.framework.bukkit.events.player.PlayerPostJoinEvent;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.player.event.PlayerDataLoadEvent;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.data.DataHandler;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

@Component
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinScoreboard(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (Imanity.BOARD_HANDLER != null) {
            Imanity.BOARD_HANDLER.getOrCreateScoreboard(player);
        }

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.registerPlayerTablist(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        long time = System.currentTimeMillis();
        Imanity.TASK_CHAIN_FACTORY
                .newChain()
                .abortIf(ignored -> !player.isOnline())
                .async(object -> {
                    EntryArrayList<PlayerData, StoreDatabase> list = new EntryArrayList<>();
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        if (!database.autoLoad()) {
                            continue;
                        }

                        PlayerData playerData = (PlayerData) database.getByUuid(player.getUniqueId());

                        if (playerData == null) {
                            playerData = (PlayerData) database.load(player);
                        }

                        list.add(playerData, database);
                    }

                    return list;
                })
                .abortIf(ignored -> !player.isOnline())
                .sync(list -> {
                    for (Entry<PlayerData, StoreDatabase> entry : list) {
                        Metadata
                                .provideForPlayer(player)
                                .put(entry.getValue().getMetadataTag(), entry.getKey());

                        PlayerDataLoadEvent.callEvent(player, entry.getKey());
                    }
                    return null;
                })
                .abortIf(ignored -> !player.isOnline())
                .sync(() -> {
                    Imanity.callEvent(new PlayerPostJoinEvent(player));
                    Imanity.LOGGER.info("Loaded PlayerData for " + player.getName() + " with " + (System.currentTimeMillis() - time) + "ms.");
                    Imanity.LOGGER.info("Player version " + player.getName() + " is " + MinecraftReflection.getProtocol(player));
                }).execute();
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

        Imanity.TASK_CHAIN_FACTORY
                .newChain()
                .async(() -> {
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        if (!database.autoSave()) {
                            return;
                        }
                        PlayerData playerData = database.getByPlayer(player);
                        playerData.disconnect();
                        database.save(playerData);
                    }
                }).sync(() -> {
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        if (!database.autoSave()) {
                            return;
                        }
                        database.delete(player.getUniqueId());
                    }

                    Events.unregisterAll(player);
                    Metadata.provideForPlayer(player.getUniqueId())
                        .clear();
                })
                .execute();
    }

}
