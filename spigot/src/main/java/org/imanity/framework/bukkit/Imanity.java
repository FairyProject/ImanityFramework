package org.imanity.framework.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.bossbar.BossBarAdapter;
import org.imanity.framework.bukkit.bossbar.BossBarHandler;
import org.imanity.framework.bukkit.chunk.KeepChunkHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetListener;
import org.imanity.framework.bukkit.command.CommandHandler;
import org.imanity.framework.bukkit.events.player.CallEventListener;
import org.imanity.framework.bukkit.hologram.HologramHandler;
import org.imanity.framework.bukkit.hologram.HologramListener;
import org.imanity.framework.bukkit.impl.BukkitCommandExecutor;
import org.imanity.framework.bukkit.impl.BukkitEventHandler;
import org.imanity.framework.bukkit.impl.BukkitImanityBridge;
import org.imanity.framework.bukkit.impl.BukkitPlayerBridge;
import org.imanity.framework.bukkit.menu.task.MenuUpdateTask;
import org.imanity.framework.bukkit.player.BukkitPlayerData;
import org.imanity.framework.bukkit.player.PlayerListener;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardHandler;
import org.imanity.framework.bukkit.timer.TimerHandler;
import org.imanity.framework.bukkit.util.*;
import org.imanity.framework.bukkit.util.items.ItemListener;
import org.imanity.framework.util.FastRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static FastRandom RANDOM;
    public static ImanityBoardHandler BOARD_HANDLER;
    public static KeepChunkHandler KEEP_CHUNK_HANDLER;
    public static BossBarHandler BOSS_BAR_HANDLER;
    public static TimerHandler TIMER_HANDLER;
    public static Plugin PLUGIN;

    public static boolean SHUTTING_DOWN = false;

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;
        Imanity.RANDOM = new FastRandom();
        Imanity.initCommon();

        BukkitPlayerData.init();

        SpigotUtil.init();
        ReflectionUtil.init();

        Imanity.TIMER_HANDLER = new TimerHandler();
        Imanity.TIMER_HANDLER.init();

        Imanity.KEEP_CHUNK_HANDLER = new KeepChunkHandler();

        CommandHandler.init();
        MenuUpdateTask.init();

        Imanity.registerEvents(
                new PlayerListener(),
                new CacheBlockSetListener(),
                new HologramListener(),
                new ItemListener(),
                new CallEventListener()
        );
    }

    private static void initCommon() {
        ImanityCommon.init(new BukkitImanityBridge(), new BukkitPlayerBridge());
        ImanityCommon.COMMAND_EXECUTOR = new BukkitCommandExecutor();
        ImanityCommon.EVENT_HANDLER = new BukkitEventHandler();
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

    public static void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            PLUGIN.getServer().getPluginManager().registerEvents(listener, PLUGIN);
        }
    }

    public static void callEvent(Event event) {
        PLUGIN.getServer().getPluginManager().callEvent(event);
    }

    public static void registerBoardHandler(ImanityBoardAdapter adapter) {
        Imanity.BOARD_HANDLER = new ImanityBoardHandler(adapter);
    }

    public static void registerBossBarHandler(BossBarAdapter adapter, long tick) {
        Imanity.BOSS_BAR_HANDLER = new BossBarHandler(adapter, tick);
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

    public static void shutdown() {
        SHUTTING_DOWN = true;
        ImanityCommon.shutdown();
    }

}
