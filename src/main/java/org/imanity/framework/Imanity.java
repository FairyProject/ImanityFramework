package org.imanity.framework;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.config.DataConfig;
import org.imanity.framework.database.DatabaseType;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.PlayerListener;
import org.imanity.framework.util.SpigotUtil;

import java.util.Arrays;

public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static final MySQL SQL = new MySQL();
    public static final Mongo MONGO = new Mongo();
    public static DataConfig DATA_CONFIG;
    public static Plugin PLUGIN;

    public static boolean SHUTTING_DOWN = false;

    public static String METADATA_PREFIX = "Imanity_";

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;

        SpigotUtil.init();

        Imanity.DATA_CONFIG = new DataConfig();
        Imanity.DATA_CONFIG.loadAndSave();

        Imanity.SQL.generateConfig(plugin);
        if (DATA_CONFIG.isDatabaseTypeUsed(DatabaseType.MYSQL)) {
            Imanity.SQL.init();
        }

        Imanity.MONGO.generateConfig();
        if (DATA_CONFIG.isDatabaseTypeUsed(DatabaseType.MONGO)) {
            Imanity.MONGO.init();
        }

        Arrays.asList(
                new PlayerListener()
        ).forEach(listener -> PLUGIN.getServer().getPluginManager().registerEvents(listener, plugin));
    }

    public static void shutdown() {
        SHUTTING_DOWN = true;

        PlayerData.shutdown();
    }

}
