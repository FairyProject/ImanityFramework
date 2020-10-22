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
