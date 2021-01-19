package org.imanity.framework.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.UuidRepresentation;
import org.imanity.framework.*;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.mongo.configuration.AbstractMongoConfiguration;
import org.imanity.framework.ProvideConfiguration;
import org.mongojack.JacksonMongoCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "mongo", dependencies = "serializer")
public class MongoService {

    private Class<?> defaultConfiguration;
    private Map<Class<?>, MongoDatabase> databases;
    private List<MongoClient> clients;

    @PreInitialize
    public void preInit() {
        this.databases = new ConcurrentHashMap<>();
        this.clients = new ArrayList<>();

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

                databases.put(configuration.getClass(), client.getDatabase(configuration.database()));
                clients.add(client);
            }
        });
    }

    @PostDestroy
    public void stop(BeanDetails beanDetails) {
        if (!beanDetails.isStage(BeanDetails.ActivationStage.POST_INIT_CALLED)) {
            return;
        }

        if (this.clients == null) {
            return;
        }

        for (MongoClient client : this.clients) {
            try {
                client.close();
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

        MongoDatabase database = this.databases.getOrDefault(type, null);
        if (database == null) {
            throw new IllegalArgumentException("The database hasn't registered");
        }

        return JacksonMongoCollection.builder()
                .withObjectMapper(objectMapper)
                .build(database, name, tClass, UuidRepresentation.JAVA_LEGACY);
    }

}
