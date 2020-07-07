package org.imanity.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.chunk.KeepChunkHandler;
import org.imanity.framework.config.DataConfig;
import org.imanity.framework.database.DatabaseType;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.menu.task.MenuUpdateTask;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.PlayerListener;
import org.imanity.framework.scoreboard.ImanityBoardAdapter;
import org.imanity.framework.scoreboard.ImanityBoardHandler;
import org.imanity.framework.scoreboard.impl.ExampleBoardAdapter;
import org.imanity.framework.util.ReflectionUtil;
import org.imanity.framework.util.SpigotUtil;

public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static final MySQL SQL = new MySQL();
    public static final Mongo MONGO = new Mongo();
    public static ImanityBoardHandler BOARD_HANDLER;
    public static KeepChunkHandler KEEP_CHUNK_HANDLER;
    public static DataConfig DATA_CONFIG;
    public static Plugin PLUGIN;

    public static boolean SHUTTING_DOWN = false;

    public static String METADATA_PREFIX = "Imanity_";

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;

        SpigotUtil.init();
        ReflectionUtil.init();

        Imanity.DATA_CONFIG = new DataConfig();
        Imanity.DATA_CONFIG.loadAndSave();

        Imanity.KEEP_CHUNK_HANDLER = new KeepChunkHandler();

        Imanity.SQL.generateConfig(plugin);
        if (DATA_CONFIG.isDatabaseTypeUsed(DatabaseType.MYSQL)) {
            Imanity.SQL.init();
        }

        Imanity.MONGO.generateConfig();
        if (DATA_CONFIG.isDatabaseTypeUsed(DatabaseType.MONGO)) {
            Imanity.MONGO.init();
        }

        Imanity.registerBoardHandler(new ExampleBoardAdapter());

        MenuUpdateTask.init();

        Imanity.registerEvents(new PlayerListener());
    }

    public static void registerEvents(Listener listener) {
        PLUGIN.getServer().getPluginManager().registerEvents(listener, PLUGIN);
    }

    public static void registerBoardHandler(ImanityBoardAdapter adapter) {
        Imanity.BOARD_HANDLER = new ImanityBoardHandler(adapter);
    }

    public static void shutdown() {
        SHUTTING_DOWN = true;

        PlayerData.shutdown();
    }

}
