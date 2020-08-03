package org.imanity.framework.redis.subscription;

import com.google.gson.JsonObject;

public interface IRedisSubscription {

    void onMessage(String payload, JsonObject jsonObject);

}
