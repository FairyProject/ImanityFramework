package org.imanity.framework.redis.server.thread;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.ServerHandler;
import org.redisson.api.RMap;

public class PushThread extends Thread {

    private ServerHandler serverHandler;

    public PushThread(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;

        this.setDaemon(true);
        this.setName("Imanity Server Push Thread");
    }

    @Override
    public void run() {

        while (!ImanityCommon.BRIDGE.isShuttingDown()) {

            try {
                this.push();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            try {
                Thread.sleep(5000L);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }

    }

    public void shutdown() {
        ImanityServer server = serverHandler.getCurrentServer();
        this.serverHandler.getRedis()
                .getMap(ServerHandler.METADATA + ":" + server.getName())
                .clear();
    }

    private void push() {

        ImanityServer server = serverHandler.getCurrentServer();

        RMap<String, String> map = this.serverHandler.getRedis().getMap(ServerHandler.METADATA + ":" + server.getName());
        map.put("onlinePlayers", String.valueOf(server.getOnlinePlayers()));
        map.put("maxPlayers", String.valueOf(server.getMaxPlayers()));
        map.put("state", server.getServerState().name());
        map.putAll(server.getMetadata());

    }
}
