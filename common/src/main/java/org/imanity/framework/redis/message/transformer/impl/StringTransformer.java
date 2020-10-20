package org.imanity.framework.redis.message.transformer.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.message.transformer.FieldTransformer;

public class StringTransformer implements FieldTransformer<String> {
    @Override
    public void add(JsonObject object, String name, Object value) {
        object.addProperty(name, (String) value);
    }

    @Override
    public String parse(JsonObject object, Class<?> type, String name) {
        return object.get(name).getAsString();
    }
}
