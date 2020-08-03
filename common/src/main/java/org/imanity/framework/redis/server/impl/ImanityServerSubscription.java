package org.imanity.framework.redis.server.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerAction;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.redis.subscription.IRedisSubscription;

public class ImanityServerSubscription implements IRedisSubscription {

    private final ServerHandler serverHandler;

    public ImanityServerSubscription(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    @Override
    public void onMessage(String payload, JsonObject jsonObject) {
        ServerAction action = ServerAction.valueOf(payload.toUpperCase());

        if (!jsonObject.has("serverName")) {
            return;
        }

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

            case DELETE:
                if (server == null) {
                    return;
                }

                this.serverHandler.removeServer(serverName);
                break;

            case COMMAND:
                if (server == null) {
                    return;
                }

                String context = null;
                if (jsonObject.has("context")) {
                    context = jsonObject.get("context").getAsString();
                }

                if (jsonObject.has("targetServerName")) {
                    String targetServerName = jsonObject.get("targetServerName").getAsString();
                    if (!this.serverHandler.getCurrentServer().getName().equals(targetServerName)) {
                        return;
                    }
                }

                ImanityCommon.COMMAND_EXECUTOR.execute(jsonObject.get("command").getAsString(), context, jsonObject.get("executor").getAsString(), server);
                break;

            case CHANGE_STATE:
                if (server == null) {
                    return;
                }

                if (!jsonObject.has("state")) {
                    return;
                }
                ServerState newState = ServerState.valueOf(jsonObject.get("state").getAsString().toUpperCase());
                ImanityCommon.EVENT_HANDLER.onServerStateChanged(server, server.getServerState(), newState);
                server.setServerState(newState);
                break;

        }
    }
}
