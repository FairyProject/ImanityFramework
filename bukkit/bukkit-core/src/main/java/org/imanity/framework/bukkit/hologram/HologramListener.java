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

package org.imanity.framework.bukkit.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class HologramListener implements Listener {

    private final Set<Player> toUpdate = new HashSet<>();

    public HologramListener() {
        Imanity.registerMovementListener(new MovementListener() {
            @Override
            public void handleUpdateLocation(Player player, Location from, Location to) {
                toUpdate.add(player);
            }

            @Override
            public void handleUpdateRotation(Player player, Location from, Location to) {

            }
        }).ignoreSameBlockAndY();

        TaskUtil.runRepeated(() -> {
            if (Imanity.SHUTTING_DOWN) {
                return;
            }
            toUpdate.removeIf(player -> {
                this.update(player);
                return true;
            });
        }, 20L);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom(), to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        this.update(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.update(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.toUpdate.remove(player);
        Imanity.getHologramHandler(player.getWorld()).reset(player);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        Metadata.provideForWorld(event.getWorld()).put(HologramHandler.WORLD_METADATA, new HologramHandler());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Metadata.provideForWorld(event.getWorld()).remove(HologramHandler.WORLD_METADATA);
    }

    private void update(Player player) {
        Imanity.getHologramHandler(player.getWorld()).update(player);
    }

}
