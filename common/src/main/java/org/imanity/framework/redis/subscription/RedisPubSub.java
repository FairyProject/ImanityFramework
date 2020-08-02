package org.imanity.framework.redis.subscription;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.imanity.framework.redis.ImanityRedis;
import org.redisson.api.RTopic;

@Getter
public class RedisPubSub {

    private final String name;
    private final RTopic topic;

    public RedisPubSub(String name, ImanityRedis redis) {
        this.name = name;
        this.topic = redis.getClient().getTopic(name);
    }

    public void subscribe(IRedisSubscription subscription) {
        this.topic.addListenerAsync(JsonObject.class, (channel, jsonObject) -> subscription.onMessage(channel.toString(), jsonObject));
    }

    public void publish(JsonObject jsonObject) {
        this.topic.publishAsync(jsonObject);
    }

    public void disable() {
        this.topic.removeAllListeners();
    }

}
