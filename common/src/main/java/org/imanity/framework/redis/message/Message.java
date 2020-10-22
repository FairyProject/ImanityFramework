/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
