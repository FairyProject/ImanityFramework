package me.skymc.taboolib.mysql.builder.hikari;

import com.zaxxer.hikari.HikariDataSource;
import me.skymc.taboolib.mysql.builder.SQLHost;

import java.util.concurrent.atomic.AtomicInteger;


public class MapDataSource {

    private SQLHost sqlHost;
    private AtomicInteger activePlugin;
    private HikariDataSource hikariDataSource;

    MapDataSource(SQLHost sqlHost, HikariDataSource hikariDataSource) {
        this.sqlHost = sqlHost;
        this.activePlugin = new AtomicInteger();
        this.hikariDataSource = hikariDataSource;
    }

    public SQLHost getSqlHost() {
        return sqlHost;
    }

    public AtomicInteger getActivePlugin() {
        return activePlugin;
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}
