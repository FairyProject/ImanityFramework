package org.imanity.framework.redis.server.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerAction;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.subscription.IRedisSubscription;

import java.util.Map;

public class ImanityServerSubscription implements IRedisSubscription {

    private ServerHandler serverHandler;

    public ImanityServerSubscription(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void onMessage(String actionString, JsonObject jsonObject) {
        ServerAction action = ServerAction.valueOf(actionString.toUpperCase());

        String serverName = jsonObject.get("serverName").getAsString();
        ImanityServer server = this.serverHandler.getServer(serverName);

        switch (action) {

            case ADD:
                if (server != null) {
                    return;
                }

                server = new ImanityServer(serverName);
                this.serverHandler.addServer(serverName, server);

                server.load(this.serverHandler.getRedis().getMap(serverName));
                break;

        }
    }
}
