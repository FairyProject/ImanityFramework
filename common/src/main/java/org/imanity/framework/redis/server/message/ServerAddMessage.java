package org.imanity.framework.redis.server.message;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.imanity.framework.redis.message.Message;
import org.imanity.framework.util.JsonChain;

@Getter
@NoArgsConstructor
public class ServerAddMessage implements Message {

    private String serverName;

    public ServerAddMessage(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public int id() {
        return 1;
    }

    @Override
    public JsonObject serialize() {
        return new JsonChain()
                .addProperty("serverName", serverName)
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        this.serverName = jsonObject.get("serverName").getAsString();
    }
}
