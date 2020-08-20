package org.imanity.framework.redis.message;

import com.google.gson.JsonObject;

public interface Message {

    int id();

    JsonObject serialize();

    void deserialize(JsonObject jsonObject);

}
