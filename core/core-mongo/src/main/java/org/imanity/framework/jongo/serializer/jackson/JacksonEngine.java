/*
 * Copyright (C) 2011 Benoît GUÉROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.imanity.framework.jongo.serializer.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imanity.framework.jongo.bson.Bson;
import org.imanity.framework.jongo.bson.BsonDocument;
import org.imanity.framework.jongo.serializer.Serializer;
import org.imanity.framework.jongo.serializer.SerializingException;
import org.imanity.framework.jongo.serializer.Deserializer;
import org.imanity.framework.jongo.serializer.jackson.configuration.Mapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class JacksonEngine implements Deserializer, Serializer {

    private final Mapping mapping;

    public JacksonEngine(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @deprecated Use {@link Mapping#getObjectMapper()} instead
     */
    @Deprecated
    public ObjectMapper getObjectMapper() {
        return mapping.getObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public <T> T unmarshall(BsonDocument document, Class<T> clazz) throws SerializingException {

        try {
            return (T) mapping.getReader(clazz).readValue(document.toByteArray(), 0, document.getSize());
        } catch (IOException e) {
            String message = String.format("Unable to unmarshall result to %s from content %s", clazz, document.toString());
            throw new SerializingException(message, e);
        }
    }

    public BsonDocument serialize(Object pojo) throws SerializingException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            mapping.getWriter(pojo).writeValue(output, pojo);
        } catch (IOException e) {
            throw new SerializingException("Unable to marshall " + pojo + " into bson", e);
        }
        return Bson.createDocument(output.toByteArray());
    }
}
