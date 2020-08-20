package org.imanity.framework.redis.server.message;

import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;

@NoArgsConstructor
public class ServerDeleteMessage extends ServerMessage {

    public ServerDeleteMessage(ImanityServer server) {
        this.setServer(server);
    }

    @Override
    public int id() {
        return 2;
    }

    @Override
    public JsonObject serialize() {
        return this.getServer()
                .json()
                .get();
    }
}
