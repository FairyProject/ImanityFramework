package org.imanity.framework;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.imanity.framework.config.CoreConfig;
import org.imanity.framework.database.DatabaseType;
import org.imanity.framework.database.Mongo;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.player.PlayerBridge;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.redis.ImanityRedis;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImanityCommon {

    public static ImanityBridge BRIDGE;
    public static CoreConfig CORE_CONFIG;
    public static LocaleHandler LOCALE_HANDLER;
    public static String METADATA_PREFIX = "Imanity_";

    public static ImanityRedis REDIS;

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

        if (ImanityCommon.CORE_CONFIG.USE_REDIS) {
            REDIS = new ImanityRedis();
            REDIS.init();
        }
    }

    public static String translate(Object player, String key) {
        LocaleData localeData = PlayerData.getPlayerData(player, LocaleData.class);
        Locale locale;
        if (localeData == null || localeData.getLocale() == null) {
            locale = ImanityCommon.LOCALE_HANDLER.getDefaultLocale();
        } else {
            locale = localeData.getLocale();
        }
        return locale.get(key);
    }

}
