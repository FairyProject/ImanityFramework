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

package org.imanity.framework.redis.subscription;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.imanity.framework.redis.ImanityRedis;
import org.redisson.api.RTopic;

@Getter
public class RedisPubSub {
    private static final Gson GSON = new Gson();

    private final String name;
    private final RTopic topic;

    public RedisPubSub(String name, ImanityRedis redis) {
        this.name = name;
        this.topic = redis.getClient().getTopic(name);
    }

    public void subscribe(IRedisSubscription subscription) {
        this.topic.addListenerAsync(String.class, (channel, message) -> {
            JsonObject object = GSON.fromJson(message, JsonObject.class).getAsJsonObject();
            String payload = object.get("payload").getAsString();
            JsonObject data = object.get("data").getAsJsonObject();
            subscription.onMessage(payload, data);
        });
    }

    public void publish(String payload, JsonObject data) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("payload", payload);
        payloadObject.add("data", data == null ? new JsonObject() : data);
        this.topic.publishAsync(payloadObject);
    }

    public void publish(int id, JsonObject data) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("payload", String.valueOf(id));
        payloadObject.add("data", data == null ? new JsonObject() : data);
        this.topic.publishAsync(payloadObject);
    }

    public void publish(Enum payload, JsonObject data) {
        this.publish(payload.name(), data);
    }

    public void disable() {
        this.topic.removeAllListeners();
    }

}
