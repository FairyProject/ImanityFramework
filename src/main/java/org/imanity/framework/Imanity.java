package org.imanity.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.Plugin;
import org.imanity.framework.config.DataConfig;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.util.SpigotUtil;

public class Imanity {

    public static final Logger LOGGER = LogManager.getLogger("Imanity");
    public static final MySQL SQL = new MySQL();
    public static final Mongo MONGO = new Mongo();
    public static DataConfig DATA_CONFIG;
    public static Plugin PLUGIN;

    public static String METADATA_PREFIX = "Imanity_";

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;

        SpigotUtil.init();

        Imanity.DATA_CONFIG = new DataConfig();
        Imanity.DATA_CONFIG.loadAndSave();

        Imanity.SQL.init(plugin);

        Imanity.MONGO.init();
    }

    public static void shutdown() {

    }

}
