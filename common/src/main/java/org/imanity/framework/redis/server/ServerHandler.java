package org.imanity.framework.redis.server;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.ImanityRedis;
import org.imanity.framework.redis.server.impl.ImanityServerSubscription;
import org.imanity.framework.redis.server.thread.FetchThread;
import org.imanity.framework.redis.subscription.RedisPubSub;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ServerHandler {

    public static final String METADATA = ImanityCommon.METADATA_PREFIX + "Server";

    private final Map<String, ImanityServer> servers = new HashMap<>();
    private final ImanityRedis redis;
    private FetchThread fetchThread;

    private RedisPubSub subscriber;

    public ServerHandler(ImanityRedis redis) {
        this.redis = redis;
    }

    public void init() {
        this.fetchThread = new FetchThread(this);
        this.fetchThread.start();

        this.subscriber = new RedisPubSub("imanity", ImanityCommon.REDIS);
        this.subscriber.subscribe(new ImanityServerSubscription(this));
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
