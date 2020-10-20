package org.imanity.framework.redis.message.transformer.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.message.transformer.FieldTransformer;

public class BooleanTransformer implements FieldTransformer<Boolean> {
    @Override
    public void add(JsonObject object, String name, Object value) {
        object.addProperty(name, (Boolean) value);
    }

    @Override
    public Boolean parse(JsonObject object, Class<?> type, String name) {
        return object.get(name).getAsBoolean();
    }
}
