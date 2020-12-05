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

package org.imanity.framework.util.builder.hikari;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.imanity.framework.util.builder.SQLHost;
import org.imanity.framework.ImanityCommon;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;


public class HikariHandler {

    private static ConcurrentHashMap<SQLHost, MapDataSource> dataSource = new ConcurrentHashMap<>();
    private static JsonObject settings = null;

    public static void init() {
        File file;
        file = new File(ImanityCommon.PLATFORM.getDataFolder(), "HikariSettings.json");
        if (!file.exists()) {
            ImanityCommon.PLATFORM.saveResources("HikariSettings.json", true);
        }
        try {
            JsonElement parse = new JsonParser().parse(new FileReader(file));
            if (parse instanceof JsonObject) {
                settings = (JsonObject) parse;
            } else {
            }
        } catch (FileNotFoundException e) {
        }
    }

    public static void closeDataSourceForce() {
        dataSource.values().forEach(x -> x.getHikariDataSource().close());
    }

    public static void closeDataSource(SQLHost host) {
        if (host != null && dataSource.containsKey(host)) {
            MapDataSource mapDataSource = dataSource.get(host);
            if (mapDataSource.getActivePlugin().getAndDecrement() <= 1) {
                mapDataSource.getHikariDataSource().close();
                dataSource.remove(host);
            } else {
            }
        }
    }

    public static DataSource createDataSource(SQLHost host) {
        MapDataSource mapDataSource = dataSource.computeIfAbsent(host, x -> new MapDataSource(x, new HikariDataSource(createConfig(host))));
        mapDataSource.getActivePlugin().getAndIncrement();
        if (mapDataSource.getActivePlugin().get() == 1) {
        } else {
        }
        return mapDataSource.getHikariDataSource();
    }

    public static HikariConfig createConfig(SQLHost sqlHost) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(getStringOrDefault("DefaultSettings.DriverClassName", "com.mysql.jdbc.Driver"));
        config.setJdbcUrl(sqlHost.getConnectionUrl());
        config.setUsername(sqlHost.getUser());
        config.setPassword(sqlHost.getPassword());
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(getBooleanOrDefault("DefaultSettings.AutoCommit", true));
        config.setMinimumIdle(getIntOrDefault("DefaultSettings.MinimumIdle", 1));
        config.setMaximumPoolSize(getIntOrDefault("DefaultSettings.MaximumPoolSize", 10));
        config.setValidationTimeout(getIntOrDefault("DefaultSettings.ValidationTimeout", 3000));
        config.setConnectionTimeout(getIntOrDefault("DefaultSettings.ConnectionTimeout", 10000));
        config.setIdleTimeout(getIntOrDefault("DefaultSettings.IdleTimeout", 60000));
        config.setMaxLifetime(getIntOrDefault("DefaultSettings.MaxLifetime", 60000));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        return config;
    }

    private static String getStringOrDefault(String node, String def) {
        return settings.has(node) ? settings.get(node).getAsString() : def;
    }

    private static int getIntOrDefault(String node, int def) {
        return settings.has(node) ? settings.get(node).getAsInt() : def;
    }

    private static boolean getBooleanOrDefault(String node, boolean def) {
        return settings.has(node) ? settings.get(node).getAsBoolean() : def;
    }
}
