/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.imanity.framework.*;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.mongo.configuration.AbstractMongoConfiguration;
import org.imanity.framework.ProvideConfiguration;
import org.mongojack.JacksonMongoCollection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "mongo", dependencies = "serializer")
public class MongoService {

    private Class<?> defaultConfiguration;
    private Map<Class<?>, MongoFactory> databases;

    @PreInitialize
    public void preInit() {
        this.databases = new ConcurrentHashMap<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {AbstractMongoConfiguration.class};
            }

            @Override
            public void onEnable(Object instance) {
                AbstractMongoConfiguration configuration = (AbstractMongoConfiguration) instance;
                if (!configuration.shouldActivate()) {
                    return;
                }

                if (defaultConfiguration == null) {
                    defaultConfiguration = configuration.getClass();
                }

                MongoClientSettings clientSettings = configuration.mongoClientSettings();
                MongoClient client = MongoClients.create(clientSettings);

                // TODO: a way to reduce client count by checking if configuration is the same target
                databases.put(configuration.getClass(), new MongoFactory(client, client.getDatabase(configuration.database())));
            }

            @Override
            public void onDisable(Object instance) {
                shutdownDatabase(instance.getClass());
            }
        });
    }

    private void shutdownDatabase(Class<?> configuration) {
        MongoFactory mongoFactory = this.databases.get(configuration);

        if (mongoFactory != null) {
            try {
                mongoFactory.getClient().close();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            this.databases.remove(configuration);
        }
    }

    @PostDestroy
    public void stop(BeanDetails beanDetails) {
        if (!beanDetails.isStage(BeanDetails.ActivationStage.POST_INIT_CALLED)) {
            return;
        }

        for (Class<?> configuration : this.databases.keySet()) {
            try {
                this.shutdownDatabase(configuration);
            } catch (Throwable ignored) {
            }
        }
    }

    public <T> JacksonMongoCollection<T> collection(String name, Class<?> use, Class<T> tClass) {
        return this.collection(name, use, tClass, FrameworkMisc.JACKSON_MAPPER);
    }

    public <T> JacksonMongoCollection<T> collection(String name, Class<?> use, Class<T> tClass, ObjectMapper objectMapper) {
        Class<?> type;
        ProvideConfiguration configuration = use.getAnnotation(ProvideConfiguration.class);
        if (configuration != null) {
            type = configuration.value();
        } else {
            type = this.defaultConfiguration;

            if (type == null) {
                throw new IllegalArgumentException("There is no mongo configuration registered!");
            }
        }

        if (!AbstractMongoConfiguration.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("The type " + type.getSimpleName() + " wasn't implemented on AbstractMongoConfiguration!");
        }

        MongoFactory mongoFactory = this.databases.getOrDefault(type, null);
        if (mongoFactory == null) {
            throw new IllegalArgumentException("The database hasn't registered");
        }

        return JacksonMongoCollection.builder()
                .withObjectMapper(objectMapper)
                .build(mongoFactory.getDatabase(), name, tClass, UuidRepresentation.JAVA_LEGACY);
    }

}
