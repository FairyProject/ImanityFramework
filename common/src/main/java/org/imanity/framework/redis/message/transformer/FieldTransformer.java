package org.imanity.framework.redis.message.transformer;

import com.google.gson.JsonObject;

public interface FieldTransformer<T> {
    void add(JsonObject object, String name, Object value);

    T parse(JsonObject object, Class<?> type, String name);
}
