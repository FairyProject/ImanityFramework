package org.imanity.framework;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.chunk.KeepChunkHandler;
import org.imanity.framework.chunk.block.CacheBlockSetHandler;
import org.imanity.framework.chunk.block.CacheBlockSetListener;
import org.imanity.framework.config.CoreConfig;
import org.imanity.framework.database.DatabaseType;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.hologram.HologramHandler;
import org.imanity.framework.hologram.HologramListener;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.menu.task.MenuUpdateTask;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.PlayerListener;
import org.imanity.framework.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.scoreboard.ImanityBoardHandler;
import org.imanity.framework.scoreboard.impl.ExampleBoardAdapter;
import org.imanity.framework.util.ReflectionUtil;
import org.imanity.framework.util.SpigotUtil;
import org.imanity.framework.util.Utility;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static final MySQL SQL = new MySQL();
    public static final Mongo MONGO = new Mongo();
    public static LocaleHandler LOCALE_HANDLER;
    public static ImanityBoardHandler BOARD_HANDLER;
    public static KeepChunkHandler KEEP_CHUNK_HANDLER;
    public static CoreConfig CORE_CONFIG;
    public static Plugin PLUGIN;

    public static boolean SHUTTING_DOWN = false;

    public static String METADATA_PREFIX = "Imanity_";

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;

        SpigotUtil.init();
        ReflectionUtil.init();

        Imanity.CORE_CONFIG = new CoreConfig();
        Imanity.CORE_CONFIG.loadAndSave();

        Imanity.LOCALE_HANDLER = new LocaleHandler();
        Imanity.LOCALE_HANDLER.init();

        Imanity.KEEP_CHUNK_HANDLER = new KeepChunkHandler();

        Imanity.SQL.generateConfig(plugin);
        if (CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MYSQL)) {
            Imanity.SQL.init();
        }

        Imanity.MONGO.generateConfig();
        if (CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MONGO)) {
            Imanity.MONGO.init();
        }

        Imanity.registerBoardHandler(new ExampleBoardAdapter());

        MenuUpdateTask.init();

        Imanity.registerEvents(
                new PlayerListener(),
                new CacheBlockSetListener(),
                new HologramListener()
        );
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

    public static void registerBoardHandler(ImanityBoardAdapter adapter) {
        Imanity.BOARD_HANDLER = new ImanityBoardHandler(adapter);
    }

    public static String translate(Player player, String key) {
        LocaleData localeData = PlayerData.getPlayerData(player, LocaleData.class);
        Locale locale;
        if (localeData == null || localeData.getLocale() == null) {
            locale = Imanity.LOCALE_HANDLER.getDefaultLocale();
        } else {
            locale = localeData.getLocale();
        }
        return Utility.color(locale.get(key));
    }

    public static Iterable<String> translateList(Player player, String key) {
        return Utility.toStringList(Imanity.translate(player, key), "\n");
    }

    public static void shutdown() {
        SHUTTING_DOWN = true;

        PlayerData.shutdown();
    }

}
