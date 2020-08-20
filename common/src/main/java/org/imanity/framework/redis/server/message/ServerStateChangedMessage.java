package org.imanity.framework.redis.server.message;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerState;

@Getter
@Setter
@NoArgsConstructor
public class ServerStateChangedMessage extends ServerMessage {

    private ServerState state;

    public ServerStateChangedMessage(ImanityServer server, ServerState state) {
        this.setServer(server);
        this.state = state;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public JsonObject serialize() {
        return this.getServer()
                .json()
                .addProperty("state", state.name())
                .get();
    }

    @Override
    public void deserialize(JsonObject jsonObject) {
        super.deserialize(jsonObject);

        this.state = ServerState.valueOf(jsonObject.get("state").getAsString().toUpperCase());

    }
}
