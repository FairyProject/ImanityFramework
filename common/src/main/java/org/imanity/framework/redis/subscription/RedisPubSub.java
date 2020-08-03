package org.imanity.framework.redis.subscription;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.imanity.framework.redis.ImanityRedis;
import org.redisson.api.RTopic;

@Getter
public class RedisPubSub {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String name;
    private final RTopic topic;

    public RedisPubSub(String name, ImanityRedis redis) {
        this.name = name;
        this.topic = redis.getClient().getTopic(name);
    }

    public void subscribe(IRedisSubscription subscription) {
        this.topic.addListenerAsync(String.class, (channel, message) -> {
            JsonObject object = JSON_PARSER.parse(message).getAsJsonObject();
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

    public void publish(Enum payload, JsonObject data) {
        JsonObject payloadObject = new JsonObject();
        payloadObject.addProperty("payload", payload.name());
        payloadObject.add("data", data == null ? new JsonObject() : data);
        this.topic.publishAsync(payloadObject);
    }

    public void disable() {
        this.topic.removeAllListeners();
    }

}
