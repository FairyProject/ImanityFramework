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

package org.imanity.framework.jongo;

import com.mongodb.DBObject;
import org.imanity.framework.jongo.bson.Bson;
import org.imanity.framework.jongo.bson.BsonDocument;
import org.imanity.framework.jongo.serializer.Deserializer;

class ResultHandlerFactory {


    public static <T> ResultHandler<T> newResultHandler(final Class<T> clazz, final Deserializer deserializer) {
        return new UnmarshallingResultHandler<T>(deserializer, clazz);
    }

    private static class UnmarshallingResultHandler<T> implements ResultHandler<T> {

        private final Deserializer deserializer;
        private final Class<T> clazz;

        public UnmarshallingResultHandler(Deserializer deserializer, Class<T> clazz) {
            this.deserializer = deserializer;
            this.clazz = clazz;
        }

        public T map(DBObject result) {
            BsonDocument bsonDocument = Bson.createDocument(result);
            return deserializer.unmarshall(bsonDocument, clazz);
        }
    }

    private ResultHandlerFactory() {
    }
}
