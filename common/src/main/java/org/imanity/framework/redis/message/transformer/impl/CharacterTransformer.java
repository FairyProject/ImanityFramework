package org.imanity.framework.redis.message.transformer.impl;

import com.google.gson.JsonObject;
import org.imanity.framework.redis.message.transformer.FieldTransformer;

public class CharacterTransformer implements FieldTransformer<Character> {
    @Override
    public void add(JsonObject object, String name, Object value) {
        object.addProperty(name, (Character) value);
    }

    @Override
    public Character parse(JsonObject object, Class<?> type, String name) {
        return object.get(name).getAsCharacter();
    }
}
