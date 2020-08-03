package org.imanity.framework.redis.server.thread;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.ServerHandler;

import java.util.Map;

public class FetchThread extends Thread {

    private ServerHandler serverHandler;

    public FetchThread(ServerHandler serverHandler) {
        super();

        this.serverHandler = serverHandler;

        this.setName("Imanity Server Fetch Thread");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while (!ImanityCommon.BRIDGE.isShuttingDown()) {
            try {
                this.fetch();
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

    private void fetch() {
        for (String key : this.serverHandler.getRedis().getKeys(ServerHandler.METADATA + ":*")) {
            String name = key.substring(0, ServerHandler.METADATA.length());
            ImanityServer server = this.serverHandler.getServer(name);
            if (server == null) {
                server = new ImanityServer(name);
                this.serverHandler.addServer(name, server);
            }

            Map<String, String> data = this.serverHandler.getRedis().getMap(key);
            server.load(data);
        }
    }
}
