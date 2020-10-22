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

    public JsonChain json() {
        return new JsonChain()
                .addProperty("serverName", this.name);
    }

}
