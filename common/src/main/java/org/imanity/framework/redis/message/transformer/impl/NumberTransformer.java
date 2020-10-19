package org.imanity.framework.redis.message.transformer.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.message.transformer.FieldTransformer;

public class NumberTransformer implements FieldTransformer<Number> {
    @Override
    public void add(JsonObject object, String name, Object value) {
        object.addProperty(name, (Number) value);
    }

    @Override
    public Number parse(JsonObject object, Class<?> type, String name) {
        return object.get(name).getAsNumber();
    }
}
