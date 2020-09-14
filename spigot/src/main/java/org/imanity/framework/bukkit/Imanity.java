package org.imanity.framework.bukkit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.github.paperspigot.Title;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.bossbar.BossBarAdapter;
import org.imanity.framework.bukkit.bossbar.BossBarHandler;
import org.imanity.framework.bukkit.chunk.KeepChunkHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetHandler;
import org.imanity.framework.bukkit.command.CommandHandler;
import org.imanity.framework.bukkit.hologram.HologramHandler;
import org.imanity.framework.bukkit.impl.*;
import org.imanity.framework.bukkit.impl.server.ServerImplementation;
import org.imanity.framework.bukkit.menu.task.MenuUpdateTask;
import org.imanity.framework.bukkit.player.BukkitPlayerData;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.player.movement.impl.AbstractMovementImplementation;
import org.imanity.framework.bukkit.player.movement.impl.BukkitMovementImplementation;
import org.imanity.framework.bukkit.player.movement.impl.ImanityMovementImplementation;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardHandler;
import org.imanity.framework.bukkit.task.BukkitTaskChainFactory;
import org.imanity.framework.bukkit.timer.TimerHandler;
import org.imanity.framework.bukkit.util.*;
import org.imanity.framework.bukkit.tablist.ImanityTabAdapter;
import org.imanity.framework.bukkit.tablist.ImanityTabHandler;
import org.imanity.framework.bukkit.visual.VisualBlockHandler;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.task.chain.TaskChainFactory;
import org.imanity.framework.util.FastRandom;

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

    public static Plugin PLUGIN;

    public static ServerImplementation IMPLEMENTATION;

    public static PluginClassLoader CLASS_LOADER;
    public static TaskChainFactory TASK_CHAIN_FACTORY;

    private static VisualBlockHandler VISUAL_BLOCK_HANDLER;

    public static List<ImanityPlugin> PLUGINS = new ArrayList<>();

    public static boolean SHUTTING_DOWN = false;

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;
        Imanity.RANDOM = new FastRandom();
        Imanity.CLASS_LOADER = new PluginClassLoader(plugin.getClass().getClassLoader());

        SpigotUtil.init();
        Imanity.IMPLEMENTATION = ServerImplementation.load();

        Imanity.initCommon();

        BukkitPlayerData.init();

        Imanity.TASK_CHAIN_FACTORY = BukkitTaskChainFactory.create(plugin);

        CommandHandler.init();
        MenuUpdateTask.init();
    }

    private static void initCommon() {
        ImanityCommon.builder()
                .bridge(new BukkitImanityBridge())
                .playerBridge(new BukkitPlayerBridge())
                .commandExecutor(new BukkitCommandExecutor())
                .eventHandler(new BukkitEventHandler())
                .taskScheduler(new BukkitTaskScheduler())
        .init();
    }

    public static VisualBlockHandler getVisualBlockHandler() {
        if (VISUAL_BLOCK_HANDLER == null) {
            VISUAL_BLOCK_HANDLER = new VisualBlockHandler();
        }

        return VISUAL_BLOCK_HANDLER;
    }

    public static CacheBlockSetHandler getBlockSetHandler(World world) {
        if (world.hasMetadata(CacheBlockSetHandler.METADATA)) {
            return (CacheBlockSetHandler) world.getMetadata(CacheBlockSetHandler.METADATA).get(0).value();
        }
        return null;
    }

    public static HologramHandler getHologramHandler(World world) {
        if (world.hasMetadata(HologramHandler.WORLD_METADATA)) {
            return (HologramHandler) world.getMetadata(HologramHandler.WORLD_METADATA).get(0).value();
        }
        throw new RuntimeException("Something wrong");
    }

    public static AbstractMovementImplementation registerMovementListener(MovementListener movementListener) {

        AbstractMovementImplementation implementation;

        switch (SpigotUtil.SPIGOT_TYPE) {

            case IMANITY:
                implementation = new ImanityMovementImplementation(movementListener);
                break;
            default:
                implementation = new BukkitMovementImplementation(movementListener);
                break;

        }

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
        return Utility.color(ImanityCommon.translate(player, key));
    }

    public static Iterable<String> translateList(Player player, String key) {
        return Utility.toStringList(Imanity.translate(player, key), "\n");
    }

    public static String translate(Player player, String key, RV... replaceValues) {
        return Utility.replace(Imanity.translate(player, key), replaceValues);
    }

    public static Iterable<String> translateList(Player player, String key, RV... replaceValues) {
        return Utility.toStringList(Imanity.translate(player, key, replaceValues), "\n");
    }

    public static String translate(Player player, String key, LocaleRV... replaceValues) {

        String result = Imanity.translate(player, key);

        for (LocaleRV rv : replaceValues) {
            result = Utility.replace(result, rv.getTarget(), rv.getReplacement(player));
        }

        return result;
    }

    public static Iterable<String> translateList(Player player, String key, LocaleRV... replaceValues) {
        return Utility.toStringList(Imanity.translate(player, key, replaceValues), "\n");
    }


    public static void broadcast(String key, LocaleRV... rvs) {
        Imanity.broadcast(Imanity.PLUGIN.getServer().getOnlinePlayers(), key, rvs);
    }

    public static void broadcast(Iterable<? extends Player> players, String key, LocaleRV... rvs) {
        for (Player player : players) {
            String result = Imanity.translate(player, key);

            for (LocaleRV rv : rvs) {
                result = Utility.replace(result, rv.getTarget(), rv.getReplacement(player));
            }

            player.sendMessage(result);
        }
    }

    public static void broadcastWithSound(String key, Sound sound, LocaleRV... rvs) {
        Imanity.broadcastWithSound(Imanity.PLUGIN.getServer().getOnlinePlayers(), key, sound, rvs);
    }

    public static void broadcastWithSound(Iterable<? extends Player> players, String key, Sound sound, LocaleRV... rvs) {
        for (Player player : players) {
            String result = Imanity.translate(player, key);

            for (LocaleRV rv : rvs) {
                result = Utility.replace(result, rv.getTarget(), rv.getReplacement(player));
            }

            player.sendMessage(result);
            player.playSound(player.getLocation(), sound, 1f, 1f);
        }
    }

    public static void broadcastTitleWithSound(String messageLocale, String titleLocale, String subTitleLocale, Sound sound, LocaleRV... rvs) {
        Imanity.broadcastTitleWithSound(Bukkit.getOnlinePlayers(), messageLocale, titleLocale, subTitleLocale, sound, rvs);
    }

    public static void broadcastTitleWithSound(Iterable<? extends Player> players, String messageLocale, String titleLocale, String subTitleLocale, Sound sound, LocaleRV... rvs) {
        players
                .forEach(player -> {
                    player.playSound(player.getLocation(), sound, 1f, 1f);
                    player.sendMessage(Imanity.translate(player, messageLocale, rvs));
                    if (SpigotUtil.getProtocolVersion(player) > 5) {
                        player.sendTitle(new Title(Imanity.translate(player, titleLocale, rvs), Imanity.translate(player, subTitleLocale, rvs), 20, 100, 20));
                    }
                });
    }

    public static void shutdown() {
        SHUTTING_DOWN = true;

        if (Imanity.TAB_HANDLER != null) {
            Imanity.TAB_HANDLER.stop();
        }
        ImanityCommon.shutdown();
    }

}
