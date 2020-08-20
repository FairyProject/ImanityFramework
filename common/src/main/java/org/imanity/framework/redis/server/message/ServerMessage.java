package org.imanity.framework.redis.server.message;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.message.Message;
import org.imanity.framework.redis.server.ImanityServer;

@Getter
@Setter
public abstract class ServerMessage implements Message {

    private ImanityServer server;

    public ServerMessage() {
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        this.server = ImanityCommon.REDIS.getServerHandler().getServer(jsonObject.get("serverName").getAsString());
    }
}
