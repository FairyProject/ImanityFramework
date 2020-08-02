package org.imanity.framework.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.ImanityBridge;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.bossbar.BossBarAdapter;
import org.imanity.framework.bukkit.bossbar.BossBarHandler;
import org.imanity.framework.bukkit.chunk.KeepChunkHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetHandler;
import org.imanity.framework.bukkit.chunk.block.CacheBlockSetListener;
import org.imanity.framework.bukkit.hologram.HologramHandler;
import org.imanity.framework.bukkit.hologram.HologramListener;
import org.imanity.framework.bukkit.menu.task.MenuUpdateTask;
import org.imanity.framework.bukkit.player.BukkitPlayerData;
import org.imanity.framework.player.PlayerBridge;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.bukkit.player.PlayerListener;
import org.imanity.framework.player.data.store.StoreDatabase;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.bukkit.scoreboard.ImanityBoardHandler;
import org.imanity.framework.bukkit.scoreboard.impl.ExampleBoardAdapter;
import org.imanity.framework.bukkit.timer.TimerHandler;
import org.imanity.framework.bukkit.util.ReflectionUtil;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.Utility;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static ImanityBoardHandler BOARD_HANDLER;
    public static KeepChunkHandler KEEP_CHUNK_HANDLER;
    public static BossBarHandler BOSS_BAR_HANDLER;
    public static TimerHandler TIMER_HANDLER;
    public static Plugin PLUGIN;

    public static boolean SHUTTING_DOWN = false;

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;
        Imanity.initCommon();

        BukkitPlayerData.init();

        SpigotUtil.init();
        ReflectionUtil.init();

        Imanity.TIMER_HANDLER = new TimerHandler();
        Imanity.TIMER_HANDLER.init();

        Imanity.KEEP_CHUNK_HANDLER = new KeepChunkHandler();

        Imanity.registerBoardHandler(new ExampleBoardAdapter());

        MenuUpdateTask.init();

        Imanity.registerEvents(
                new PlayerListener(),
                new CacheBlockSetListener(),
                new HologramListener()
        );
    }

    private static void initCommon() {
        ImanityCommon.init(new ImanityBridge() {
            @Override
            public File getDataFolder() {
                return PLUGIN.getDataFolder();
            }

            @Override
            public Logger getLogger() {
                return LOGGER;
            }

            @Override
            public Map<String, Object> loadYaml(File file) {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                return configuration.getValues(true);
            }
            @Override
            public void saveResources(String name, boolean replace) {
                PLUGIN.saveResource(name, replace);
            }
        }, new PlayerBridge<Player>() {
            @Override
            public PlayerData getPlayerData(Player player, StoreDatabase database) {
                return (PlayerData) player.getMetadata(database.getMetadataTag()).get(0).value();
            }
            @Override
            public Collection<? extends Player> getOnlinePlayers() {
                return Imanity.PLUGIN.getServer().getOnlinePlayers();
            }
            @Override
            public UUID getUUID(Player player) {
                return player.getUniqueId();
            }

            @Override
            public String getName(Player player) {
                return player.getName();
            }

            @Override
            public Class<Player> getPlayerClass() {
                return Player.class;
            }
        });
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

    public static void shutdown() {
        SHUTTING_DOWN = true;

        PlayerData.shutdown();
    }

}
