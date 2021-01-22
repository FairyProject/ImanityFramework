/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.redis.server.message.listener;

import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.message.MessageListener;
import org.imanity.framework.redis.message.annotation.HandleMessage;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.redis.server.message.ServerAddMessage;
import org.imanity.framework.redis.server.message.ServerCommandMessage;
import org.imanity.framework.redis.server.message.ServerDeleteMessage;
import org.imanity.framework.redis.server.message.ServerStateChangedMessage;

@Component
public class ServerListener implements MessageListener {

    @Autowired
    private ServerHandler serverHandler;

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

        if (!this.serverHandler.getCurrentServer().getName().equals(message.getTarget())) {
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
        server.setServerState(newState);
    }

}
