/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.redis.server;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.redis.RedisService;
import org.imanity.framework.redis.message.MessageService;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.redis.server.message.ServerDeleteMessage;
import org.imanity.framework.redis.server.message.ServerStateChangedMessage;
import org.imanity.framework.redis.server.thread.FetchThread;
import org.imanity.framework.redis.server.thread.PushThread;

import java.util.HashMap;
import java.util.Map;

@Service(name = "serverHandler", dependencies = "redis")
@Getter
public class ServerHandler implements IService {

    public static final String METADATA = ImanityCommon.METADATA_PREFIX + "Server";

    private final Map<String, ImanityServer> servers = new HashMap<>();

    private FetchThread fetchThread;
    private PushThread pushThread;

    private ImanityServer currentServer;

    @Autowired
    private RedisService redis;
    @Autowired
    private MessageService messageService;

    @Override
    public void init() {
        this.currentServer = new ImanityServer(ImanityCommon.CORE_CONFIG.CURRENT_SERVER);
        this.currentServer.setServerState(ServerState.BOOTING);

        this.fetchThread = new FetchThread(this);
        this.fetchThread.start();

        this.pushThread = new PushThread(this);
        this.pushThread.start();
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

    public void changeServerState(ServerState serverState) {
        ServerState previousState = this.currentServer.getServerState();
        if (serverState == previousState) {
            return;
        }

        this.currentServer.setServerState(serverState);
        this.messageService.sendMessage(new ServerStateChangedMessage(this.currentServer, serverState));
    }

    public void addMetadata(String key, String value) {
        this.getCurrentServer().getMetadata().put(key, value);
    }

    public void removeMetadata(String key) {
        this.getCurrentServer().getMetadata().remove(key);
    }

    @Override
    public void stop() {
        this.messageService.sendMessage(new ServerDeleteMessage(this.currentServer));
        this.pushThread.shutdown();

        this.pushThread.interrupt();
        this.fetchThread.interrupt();
    }

}
