package org.imanity.framework.redis.message;

import com.google.gson.JsonObject;
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

                        if (Number.class.isAssignableFrom(field.getType())) {
                            object.addProperty(field.getName(), (Number) value);
                        } else if (String.class.isAssignableFrom(field.getType())) {
                            object.addProperty(field.getName(), (String) value);
                        } else if (Boolean.class.isAssignableFrom(field.getType())) {
                            object.addProperty(field.getName(), (Boolean) value);
                        } else if (Character.class.isAssignableFrom(field.getType())) {
                            object.addProperty(field.getName(), (Character) value);
                        } else if (Enum.class.isAssignableFrom(field.getType())) {
                            object.addProperty(field.getName(), ((Enum<?>) value).name());
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
                if (Number.class.isAssignableFrom(field.getType())) {
                    field.set(this, entry.getValue().getAsNumber());
                } else if (String.class.isAssignableFrom(field.getType())) {
                    field.set(this, entry.getValue().getAsString());
                } else if (Boolean.class.isAssignableFrom(field.getType())) {
                    field.set(this, entry.getValue().getAsBoolean());
                } else if (Character.class.isAssignableFrom(field.getType())) {
                    field.set(this, entry.getValue().getAsCharacter());
                } else if (Enum.class.isAssignableFrom(field.getType())) {
                    field.set(this, Enum.valueOf((Class<Enum>) field.getType(), entry.getValue().getAsString()));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

}
