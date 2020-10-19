package org.imanity.framework.redis.message;

import com.google.gson.JsonObject;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.redis.ImanityRedis;
import org.imanity.framework.redis.message.transformer.FieldTransformer;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.util.AccessUtil;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

public interface Message {

    default int id() {
        return Objects.hash(this.getClass().getPackage().getName(), this.getClass().getSimpleName());
    }

    default JsonObject serialize() {
        JsonObject object = new JsonObject();
        Stream.of(this.getClass().getDeclaredFields())
                .forEach(field -> {
                    try {
                        AccessUtil.setAccessible(field);
                        Object value = field.get(this);
                        FieldTransformer<?> transformer = ImanityCommon.REDIS.getTransformer(field.getType());
                        if (transformer != null) {
                            transformer.add(object, field.getName(), value);
                        } else {
                            ImanityCommon.getLogger().error("The FieldTransformer for field " + field.getName() + " with type " + field.getType() + " does not exist");
                        }

                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                });

        return object;
    }

    default void deserialize(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            try {
                Field field = this.getClass().getDeclaredField(entry.getKey());
                AccessUtil.setAccessible(field);

                FieldTransformer<?> transformer = ImanityCommon.REDIS.getTransformer(field.getType());
                if (transformer != null) {
                    field.set(this, transformer.parse(jsonObject, field.getType(), entry.getKey()));
                } else {
                    ImanityCommon.getLogger().error("The FieldTransformer for field " + field.getName() + " with type " + field.getType() + " does not exist");
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        });
    }

}
