package org.imanity.framework.redis.server;

import lombok.Getter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.redis.ImanityRedis;
import org.imanity.framework.redis.message.MessageHandler;
import org.imanity.framework.redis.server.enums.ServerAction;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.redis.server.message.ServerAddMessage;
import org.imanity.framework.redis.server.message.ServerCommandMessage;
import org.imanity.framework.redis.server.message.ServerDeleteMessage;
import org.imanity.framework.redis.server.message.ServerStateChangedMessage;
import org.imanity.framework.redis.server.message.listener.ServerListener;
import org.imanity.framework.redis.server.thread.FetchThread;
import org.imanity.framework.redis.server.thread.PushThread;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ServerHandler {

    public static final String METADATA = ImanityCommon.METADATA_PREFIX + "Server";

    private final Map<String, ImanityServer> servers = new HashMap<>();
    private final ImanityRedis redis;

    private FetchThread fetchThread;
    private PushThread pushThread;

    private MessageHandler messageHandler;

    private ImanityServer currentServer;
    private ServerConfig serverConfig;

    public ServerHandler(ImanityRedis redis) {
        this.redis = redis;
    }

    public void init() {
        this.serverConfig = new ServerConfig();
        this.serverConfig.loadAndSave();

        this.currentServer = new ImanityServer(this.serverConfig.CURRENT_SERVER);
        this.currentServer.setServerState(ServerState.BOOTING);

        this.fetchThread = new FetchThread(this);
        this.fetchThread.start();

        this.pushThread = new PushThread(this);
        this.pushThread.start();

        this.messageHandler = new MessageHandler("imanity-server");
        this.messageHandler.registerMessage(ServerStateChangedMessage.class);
        this.messageHandler.registerMessage(ServerAddMessage.class);
        this.messageHandler.registerMessage(ServerDeleteMessage.class);
        this.messageHandler.registerMessage(ServerCommandMessage.class);
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
        this.messageHandler.sendMessage(new ServerStateChangedMessage(this.currentServer, serverState));
    }

    public void addMetadata(String key, String value) {
        this.getCurrentServer().getMetadata().put(key, value);
    }

    public void removeMetadata(String key) {
        this.getCurrentServer().getMetadata().remove(key);
    }

    public void shutdown() {
        this.messageHandler.sendMessage(new ServerDeleteMessage(this.currentServer));
        this.pushThread.shutdown();

        this.pushThread.interrupt();
        this.fetchThread.interrupt();
    }

    public static class ServerConfig extends YamlConfiguration {

        public String CURRENT_SERVER;

        protected ServerConfig() {
            super(new File(ImanityCommon.BRIDGE.getDataFolder(), "servers.yml").toPath(), YamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }
    }

}
