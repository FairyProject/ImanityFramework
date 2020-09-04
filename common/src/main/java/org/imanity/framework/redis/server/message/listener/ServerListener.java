package org.imanity.framework.redis.server.message.listener;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.message.annotation.AutoWiredMessageListener;
import org.imanity.framework.redis.message.annotation.HandleMessage;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.redis.server.message.ServerAddMessage;
import org.imanity.framework.redis.server.message.ServerCommandMessage;
import org.imanity.framework.redis.server.message.ServerDeleteMessage;
import org.imanity.framework.redis.server.message.ServerStateChangedMessage;

@AutoWiredMessageListener
public class ServerListener {

    private final ServerHandler serverHandler = ImanityCommon.REDIS.getServerHandler();

    @HandleMessage
    public void onServerAdd(ServerAddMessage message) {
        String serverName = message.getServerName();
        ImanityServer server = this.serverHandler.getServer(serverName);

        if (server != null) {
            return;
        }

        server = new ImanityServer(serverName);
        this.serverHandler.addServer(serverName, server);

        server.load(this.serverHandler.getRedis().getMap(serverName));
    }

    @HandleMessage
    public void onServerDelete(ServerDeleteMessage message) {
        ImanityServer server = message.getServer();

        if (server == null) {
            return;
        }

        this.serverHandler.removeServer(server.getName());
    }

    @HandleMessage
    public void onServerCommand(ServerCommandMessage message) {
        ImanityServer server = message.getServer();

        if (server == null) {
            return;
        }

        if (!this.serverHandler.getCurrentServer().getName().equals(message.getTargetServerName())) {
            return;
        }

        ImanityCommon.COMMAND_EXECUTOR.execute(message.getCommand(), message.getContext(), message.getExecutor(), server);
    }

    @HandleMessage
    public void onServerStateChanged(ServerStateChangedMessage message) {
        ImanityServer server = message.getServer();

        if (server == null) {
            return;
        }

        ServerState newState = message.getState();
        ImanityCommon.EVENT_HANDLER.onServerStateChanged(server, server.getServerState(), newState);
        server.setServerState(newState);
    }

}
