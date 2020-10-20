package org.imanity.framework.redis.message.transformer.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.message.transformer.FieldTransformer;

public class EnumTransformer implements FieldTransformer<Enum<?>> {
    @Override
    public void add(JsonObject object, String name, Object value) {
        object.addProperty(name, ((Enum<?>) value).name());
    }

    @Override
    public Enum<?> parse(JsonObject object, Class<?> type, String name) {
        return Enum.valueOf((Class<Enum>) type, object.get(name).getAsString());
    }
}
