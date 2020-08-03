package org.imanity.framework.redis.server;

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.util.JsonChain;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ImanityServer {

    private String name;
    private int onlinePlayers;
    private int maxPlayers;
    private ServerState serverState;

    private final Map<String, String> metadata = new HashMap<>();

    public ImanityServer(String name) {
        this.name = name;
    }

    public void load(Map<String, String> data) {
        this.onlinePlayers = Integer.parseInt(data.get("onlinePlayers"));
        this.maxPlayers = Integer.parseInt(data.get("maxPlayers"));
        this.serverState = ServerState.valueOf(data.get("state").toUpperCase());

        data.remove("onlinePlayers");
        data.remove("maxPlayers");
        data.remove("state");
        this.metadata.clear();
        this.metadata.putAll(data);
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(metadata.get(key));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public double getDouble(String key) {
        try {
            return Double.parseDouble(metadata.get(key));
        } catch (NumberFormatException ex) {
            return -1D;
        }
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(metadata.get(key));
    }

    public String getString(String key) {
        return metadata.get(key);
    }

    public JsonChain packageJson() {
        return new JsonChain()
                .addProperty("serverName", this.name);
    }

}
