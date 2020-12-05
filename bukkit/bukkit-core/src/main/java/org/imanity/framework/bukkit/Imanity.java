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

package org.imanity.framework.bukkit;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.Autowired;
import org.imanity.framework.ComponentRegistry;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.bossbar.BossBarAdapter;
import org.imanity.framework.bukkit.bossbar.BossBarHandler;
import org.imanity.framework.bukkit.chunk.KeepChunkHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetHandler;
import org.imanity.framework.bukkit.hologram.HologramHandler;
import org.imanity.framework.bukkit.impl.*;
import org.imanity.framework.bukkit.impl.server.ServerImplementation;
import org.imanity.framework.bukkit.menu.task.MenuUpdateTask;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.packet.PacketService;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutTitle;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.player.movement.impl.AbstractMovementImplementation;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.minecraft.MinecraftVersion;
import org.imanity.framework.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardHandler;
import org.imanity.framework.bukkit.impl.BukkitTaskChainFactory;
import org.imanity.framework.bukkit.timer.TimerHandler;
import org.imanity.framework.bukkit.util.*;
import org.imanity.framework.bukkit.tablist.ImanityTabAdapter;
import org.imanity.framework.bukkit.tablist.ImanityTabHandler;
import org.imanity.framework.bukkit.visual.VisualBlockHandler;
import org.imanity.framework.locale.LocaleRepository;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.plugin.PluginClassLoader;
import org.imanity.framework.task.chain.TaskChainFactory;
import org.imanity.framework.util.FastRandom;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static FastRandom RANDOM;
    public static ImanityBoardHandler BOARD_HANDLER;
    public static ImanityTabHandler TAB_HANDLER;
    public static BossBarHandler BOSS_BAR_HANDLER;

    @Autowired
    public static TimerHandler TIMER_HANDLER;

    @Autowired
    public static KeepChunkHandler KEEP_CHUNK_HANDLER;

    @Autowired
    private static LocaleRepository LOCALE_REPOSITORY;

    public static Plugin PLUGIN;

    public static ServerImplementation IMPLEMENTATION;

    public static PluginClassLoader CLASS_LOADER;
    public static TaskChainFactory TASK_CHAIN_FACTORY;

    private static VisualBlockHandler VISUAL_BLOCK_HANDLER;

    public static List<ImanityPlugin> PLUGINS = new ArrayList<>();

    public static boolean SHUTTING_DOWN = false;

    public static boolean TESTING = false;

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;
        Imanity.RANDOM = new FastRandom();
        Imanity.CLASS_LOADER = new PluginClassLoader(plugin.getClass().getClassLoader());

        SpigotUtil.init();
        Imanity.initCommon();

        Imanity.IMPLEMENTATION = ServerImplementation.load();
        Imanity.TASK_CHAIN_FACTORY = BukkitTaskChainFactory.create(plugin);

        MenuUpdateTask.init();
    }

    private static void initCommon() {
        ComponentRegistry.registerComponentHolder(new ComponentHolderBukkitListener());

        ImanityCommon.builder()
                .platform(new BukkitImanityPlatform())
                .playerBridge(new BukkitPlayerBridge())
                .commandExecutor(new BukkitCommandExecutor())
                .eventHandler(new BukkitEventHandler())
                .taskScheduler(new BukkitTaskScheduler())
        .init();

        new BukkitRepository<LocaleData>(PLUGIN)
                .async()
                .load(player -> LOCALE_REPOSITORY.find(player.getUniqueId()))
                .save((player, localeData) -> LOCALE_REPOSITORY.save(localeData))
                .init();
    }

    public static VisualBlockHandler getVisualBlockHandler() {
        if (VISUAL_BLOCK_HANDLER == null) {
            VISUAL_BLOCK_HANDLER = new VisualBlockHandler();
        }

        return VISUAL_BLOCK_HANDLER;
    }

    public static CacheBlockSetHandler getBlockSetHandler(World world) {
        return Metadata.provideForWorld(world).getOrNull(CacheBlockSetHandler.METADATA);
    }

    public static HologramHandler getHologramHandler(World world) {
        return Metadata.provideForWorld(world)
                .get(HologramHandler.WORLD_METADATA)
                .orElseThrow(() -> new RuntimeException("Something wrong while getting world hologram handler"));
    }

    public static AbstractMovementImplementation registerMovementListener(MovementListener movementListener) {

        AbstractMovementImplementation implementation = Imanity.IMPLEMENTATION.movement(movementListener);

        implementation.register();
        return implementation;

    }

    public static void registerEvents(Listener... listeners) {

        for (Listener listener : listeners) {
            Plugin plugin = null;

            try {
                plugin = JavaPlugin.getProvidingPlugin(listener.getClass());
            } catch (Throwable ignored) {}

            if (plugin == null) {
                plugin = Imanity.PLUGIN;
            }

            if (!plugin.isEnabled()) {

                if (plugin instanceof ImanityPlugin) {

                    Plugin finalPlugin = plugin;
                    ((ImanityPlugin) plugin).queue(() -> PLUGIN.getServer().getPluginManager().registerEvents(listener, finalPlugin));

                } else {

                    Imanity.LOGGER.error("The plugin hasn't enabled but trying to register listener " + listener.getClass().getSimpleName());
                    return;
                }
            } else {

                PLUGIN.getServer().getPluginManager().registerEvents(listener, plugin);

            }
        }
    }

    public static List<? extends Player> getPlayers() {
        return ImmutableList.copyOf(Imanity.PLUGIN.getServer().getOnlinePlayers());
    }

    public static void callEvent(Event event) {
        PLUGIN.getServer().getPluginManager().callEvent(event);
    }

    public static void registerBoardHandler(ImanityBoardAdapter adapter) {
        Imanity.BOARD_HANDLER = new ImanityBoardHandler(adapter);
    }

    public static void registerTablistHandler(ImanityTabAdapter adapter) {
        Imanity.TAB_HANDLER = new ImanityTabHandler(adapter);
    }

    public static void registerBossBarHandler(BossBarAdapter adapter) {
        Imanity.BOSS_BAR_HANDLER = new BossBarHandler(adapter);
    }

    public static String translate(Player player, String key) {
        return BukkitUtil.color(ImanityCommon.translate(player.getUniqueId(), key));
    }

    public static Iterable<String> translateList(Player player, String key) {
        return BukkitUtil.toStringList(Imanity.translate(player, key), "\n");
    }

    public static String translate(Player player, String key, RV... replaceValues) {
        return BukkitUtil.replace(Imanity.translate(player, key), replaceValues);
    }

    public static Iterable<String> translateList(Player player, String key, RV... replaceValues) {
        return BukkitUtil.toStringList(Imanity.translate(player, key, replaceValues), "\n");
    }

    public static String translate(Player player, String key, LocaleRV... replaceValues) {

        String result = Imanity.translate(player, key);

        for (LocaleRV rv : replaceValues) {
            result = BukkitUtil.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        return result;
    }

    public static Iterable<String> translateList(Player player, String key, LocaleRV... replaceValues) {
        return BukkitUtil.toStringList(Imanity.translate(player, key, replaceValues), "\n");
    }


    public static void broadcast(String key, LocaleRV... rvs) {
        Imanity.broadcast(Imanity.getPlayers(), key, null, null, null, rvs);
    }

    public static void broadcast(Iterable<? extends Player> players, String key, LocaleRV... rvs) {
        Imanity.broadcast(players, key, null, null, null, rvs);
    }

    public static void broadcastWithSound(String key, Sound sound, LocaleRV... rvs) {
        Imanity.broadcastWithSound(Imanity.getPlayers(), key, sound, rvs);
    }

    public static void broadcastWithSound(Iterable<? extends Player> players, String key, Sound sound, LocaleRV... rvs) {
        Imanity.broadcast(players, key, null, null, sound, rvs);
    }

    public static void broadcastTitleWithSound(String messageLocale, String titleLocale, String subTitleLocale, Sound sound, LocaleRV... rvs) {
        Imanity.broadcast(Imanity.getPlayers(), messageLocale, titleLocale, subTitleLocale, sound, rvs);
    }

    public static void broadcast(@NonNull Iterable<? extends Player> players, @Nullable String messageLocale, @Nullable String titleLocale, @Nullable String subTitleLocale, @Nullable Sound sound, LocaleRV... rvs) {

        boolean hasTitle = MinecraftVersion.VERSION.olderThan(MinecraftReflection.Version.v1_7_R4);

        players.forEach(player -> {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1f, 1f);
            }

            if (messageLocale != null) {
                player.sendMessage(Imanity.translate(player, messageLocale, rvs));
            }

            if (!hasTitle) {
                return;
            }

            if (titleLocale != null && subTitleLocale != null) {
                Imanity.sendTitle(player, Imanity.translate(player, titleLocale, rvs), Imanity.translate(player, subTitleLocale, rvs));
            } else if (titleLocale != null) {
                Imanity.sendTitle(player, Imanity.translate(player, titleLocale, rvs));
            } else if (subTitleLocale != null) {
                Imanity.sendSubTitle(player, Imanity.translate(player, subTitleLocale, rvs));
            }
        });

    }

    public static void sendSubTitle(Player player, String subTitle) {
        Imanity.sendTitle(player, null, subTitle);
    }

    public static void sendSubTitle(Player player, String subTitle, int fadeIn, int stay, int fadeOut) {
        Imanity.sendTitle(player, null, subTitle, fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, String title) {
        Imanity.sendTitle(player, title, null);
    }

    public static void sendTitle(Player player, String title, String subTitle) {
        Imanity.sendTitle(player, title, subTitle, WrappedPacketOutTitle.DEFAULT_FADE_IN, WrappedPacketOutTitle.DEFAULT_STAY, WrappedPacketOutTitle.DEFAULT_FADE_OUT);
    }

    public static void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        Imanity.sendTitle(player, title, null, fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, @Nullable String title, @Nullable String subTitle, int fadeIn, int stay, int fadeOut) {

        if (title != null) {
            PacketService.send(player, WrappedPacketOutTitle.builder()
                .action(WrappedPacketOutTitle.Action.TITLE)
                .message(ChatComponentWrapper.fromText(title))
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .build());
        }

        if (subTitle != null) {
            PacketService.send(player, WrappedPacketOutTitle.builder()
                    .action(WrappedPacketOutTitle.Action.SUBTITLE)
                    .message(ChatComponentWrapper.fromText(title))
                    .fadeIn(fadeIn)
                    .stay(stay)
                    .fadeOut(fadeOut)
                    .build());
        }

    }

    @SneakyThrows
    public static void shutdown() {
        SHUTTING_DOWN = true;

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.stop();
        }
        ImanityCommon.shutdown();
    }

}
