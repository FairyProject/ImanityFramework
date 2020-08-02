package org.imanity.framework;

import org.imanity.framework.config.CoreConfig;
import org.imanity.framework.database.DatabaseType;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.jedis.JedisHandler;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.player.PlayerBridge;
import org.imanity.framework.player.data.PlayerData;

public class ImanityCommon {

    public static ImanityBridge BRIDGE;
    public static CoreConfig CORE_CONFIG;
    public static LocaleHandler LOCALE_HANDLER;
    public static String METADATA_PREFIX = "Imanity_";

    public static JedisHandler JEDIS_HANDLER;

    public static final MySQL SQL = new MySQL();
    public static final Mongo MONGO = new Mongo();

    public static void init(ImanityBridge bridge, PlayerBridge playerBridge) {
        ImanityCommon.BRIDGE = bridge;

        ImanityCommon.CORE_CONFIG = new CoreConfig();
        ImanityCommon.CORE_CONFIG.loadAndSave();

        ImanityCommon.SQL.generateConfig();
        if (CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MYSQL)) {
            ImanityCommon.SQL.init();
        }

        ImanityCommon.MONGO.generateConfig();
        if (CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MONGO)) {
            ImanityCommon.MONGO.init();
        }

        PlayerData.PLAYER_BRIDGE = playerBridge;

        ImanityCommon.LOCALE_HANDLER = new LocaleHandler();
        ImanityCommon.LOCALE_HANDLER.init();

        if (ImanityCommon.CORE_CONFIG.USE_JEDIS) {
            JEDIS_HANDLER = new JedisHandler();
            JEDIS_HANDLER.init();
        }
    }

}
