package org.imanity.framework.redis.server.thread;

import org.imanity.framework.redis.server.ServerHandler;

public class PushThread extends Thread {

    private ServerHandler serverHandler;

    public PushThread(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;

        this.setDaemon(true);
        this.setName("Imanity Server Push Thread");
    }

    @Override
    public void run() {



    }
}
