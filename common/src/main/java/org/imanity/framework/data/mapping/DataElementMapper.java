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

package org.imanity.framework.data.mapping;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.gson.Gson;
import org.bson.Document;

public class DataElementMapper {

    private final ObjectMapper mapper;
    private final SimpleModule module;
    private final Gson gson;

    public DataElementMapper() {
        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        this.module = new SimpleModule();
        this.gson = new Gson();

        this.mapper.registerModule(module);
    }

    public <T> void registerSerializer(Class<T> type, StdSerializer<T> serializer) {
        this.module.addSerializer(type, serializer);
    }

    public <T> void registerDeserializer(Class<T> type, StdDeserializer<T> serializer) {
        this.module.addDeserializer(type, serializer);
    }

    public Document toJson(Object element) {

        try {
            return this.gson.fromJson(this.mapper.writeValueAsString(element), Document.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public <T> T fromJson(Document jsonObject, Class<T> type) {

        try {
            return this.mapper.readValue(jsonObject.toJson(), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
