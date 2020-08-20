package org.imanity.framework.redis.server.message;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.imanity.framework.redis.server.ImanityServer;

@Getter
@NoArgsConstructor
public class ServerCommandMessage extends ServerMessage {

    @Setter
    private String command;
    private String context;
    private String executor;

    private String targetServerName;

    public ServerCommandMessage(ImanityServer server, String command, String context, String executor) {
        this.setServer(server);
        this.command = command;
        this.context = context;
        this.executor = executor;
    }

    @Override
    public int id() {
        return 3;
    }

    @Override
    public JsonObject serialize() {
        return this.getServer().json()
                .addProperty("command", command)
                .addProperty("context", context)
                .addProperty("executor", executor)
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        super.deserialize(jsonObject);
        this.command = jsonObject.get("command").getAsString();
        if (jsonObject.has("context")) {
            this.context = jsonObject.get("context").getAsString();
        }
        if (jsonObject.has("targetServerName")) {
            this.targetServerName = jsonObject.get("targetServerName").getAsString();
        }
        this.executor = jsonObject.get("executor").getAsString();
    }
}
