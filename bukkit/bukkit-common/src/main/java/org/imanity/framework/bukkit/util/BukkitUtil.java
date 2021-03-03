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

package org.imanity.framework.bukkit.util;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.reflect.Reflect;
import org.imanity.framework.util.CC;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class BukkitUtil {

    public static List<Player> getPlayersFromUuids(Collection<UUID> uuids) {
        return uuids
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    public static Player getDamager(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageByEntityEvent.getDamager();

            if (damager instanceof Player) {
                return (Player) damageByEntityEvent.getDamager();
            }

            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) {
                    return (Player) shooter;
                }
            }
        }
        return null;
    }

    public static String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor){

        float percent = (float) current / max;

        int progressBars = (int) ((int) totalBars * percent);

        int leftOver = (totalBars - progressBars);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.translateAlternateColorCodes('&', completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }
        sb.append(ChatColor.translateAlternateColorCodes('&', notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static void clear(final Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0.0f);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
    }

    public static void sendMessages(Player player, Iterable<String> iterable) {
        for (String message : iterable) {
            player.sendMessage(message);
        }
    }

    public static File getPluginJar(JavaPlugin plugin) {

        MethodResolver resolver = new MethodResolver(JavaPlugin.class);
        return (File) resolver.resolveWrapper("getFile").invoke(plugin);

    }

    public static void delayedUpdateInventory(Player player) {
        TaskUtil.runScheduled(() -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        }, 1L);
    }

    public static boolean isPlayerEvent(Class<?> event) {

        if (PlayerEvent.class.isAssignableFrom(event)) {
            return true;
        }

        MethodResolver resolver = new MethodResolver(event);
        return resolver.resolveWrapper("getPlayer").exists();

    }

    public static org.bukkit.block.Block getBlockLookingAt(final Player player, final int distance) {
        final Location location = player.getEyeLocation();
        final BlockIterator blocksToAdd = new BlockIterator(location, 0.0D, distance);
        org.bukkit.block.Block block = null;
        while (blocksToAdd.hasNext()) {
            block = blocksToAdd.next();
        }
        return block;
    }

    public static File getDataFolder(int depth) {
        Class<?> caller = Reflect.getCallerClass(depth).orElse(null);

        if (caller != null) {
            Plugin plugin = null;

            try {
                plugin = JavaPlugin.getProvidingPlugin(caller);
            } catch (Throwable ignored) {}

            if (plugin != null) {
                return plugin.getDataFolder();
            } else {
                throw new IllegalArgumentException("Caller class from depth " + depth + " is not plugin class.");
            }
        } else {
            throw new IllegalArgumentException("Caller class from depth " + depth + " does not exists.");
        }
    }

    public static File getDataFolder() {
        return getDataFolder(4);
    }

    public static File getFile(String fileName) {
        return new File(getDataFolder(4), fileName);
    }

    @Deprecated
    public static String color(String msg) {
        return CC.translate(msg);
    }

    public static boolean isNPC(Player player) {
        if (player.hasMetadata("ImanityBot")) {
            return true;
        }

        if (player.hasMetadata("NPC")) {
            return true;
        }

        return false;
    }

}
