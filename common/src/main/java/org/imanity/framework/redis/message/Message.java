package org.imanity.framework.redis.message;

import com.google.gson.JsonObject;

import java.util.Objects;

public interface Message {

    default int id() {
        return Objects.hash(this.getClass().getPackage().getName(), this.getClass().getSimpleName());
    }

    JsonObject serialize();

    void deserialize(JsonObject jsonObject);

}
