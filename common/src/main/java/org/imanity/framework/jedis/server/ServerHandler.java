package org.imanity.framework.jedis.server;

import org.imanity.framework.jedis.JedisHandler;

import java.util.HashMap;
import java.util.Map;

public class ServerHandler {

    private final Map<String, ImanityServer> servers = new HashMap<>();
    private final JedisHandler jedisHandler;
    private FetchThread fetchThread;

    public ServerHandler(JedisHandler jedisHandler) {
        this.jedisHandler = jedisHandler;
    }

    public void init() {
        this.fetchThread = new FetchThread(this.jedisHandler.getJedisHelper());
        this.fetchThread.start();
    }

    public ImanityServer getServer(String name) {
        return this.servers.getOrDefault(name, null);
    }

    public void addServer(String name, ImanityServer server) {
        this.servers.put(name, server);
    }

    public void removeServer(String name) {
        this.servers.remove(name);
    }

}
