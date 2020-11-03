package org.imanity.framework;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.imanity.framework.jongo.Jongo;
import org.imanity.framework.jongo.MongoCollection;
import org.imanity.framework.jongo.configuration.AbstractMongoConfiguration;
import org.imanity.framework.jongo.configuration.ProvideConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "mongo")
public class MongoService {

    private List<AbstractMongoConfiguration> configurations;

    private Class<?> defaultConfiguration;
    private Map<Class<?>, Jongo> databases;

    @PreInitialize
    public void preInit() {
        this.configurations = new ArrayList<>(1);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {AbstractMongoConfiguration.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                AbstractMongoConfiguration instance = (AbstractMongoConfiguration) super.newInstance(type);
                configurations.add(instance);
                return instance;
            }
        });
    }

    @PostInitialize
    public void init() {
        this.databases = new ConcurrentHashMap<>(this.configurations.size());

        for (AbstractMongoConfiguration configuration : this.configurations) {
            if (this.defaultConfiguration == null) {
                this.defaultConfiguration = configuration.getClass();
            }

            ServerAddress address = configuration.serverAddress();
            MongoClientOptions clientOptions = configuration.mongoClientOptions();
            MongoCredential credential = configuration.credential();
            MongoClient client = new MongoClient(address, credential, clientOptions);

            DB database = client.getDB(configuration.database());

            this.databases.put(configuration.getClass(), new Jongo(database));
        }

        this.configurations.clear();
        this.configurations = null;
    }

    @PostDestroy
    public void stop() {
        for (Jongo jongo : this.databases.values()) {
            try {
                jongo.getDatabase().getMongoClient().close();
            } catch (Throwable ignored) {
            }
        }
    }

    public MongoCollection collection(String name, Class<?> use) {
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

        Jongo jongo = this.databases.getOrDefault(type, null);
        if (jongo == null) {
            throw new IllegalArgumentException("The database hasn't registered");
        }

        return jongo.getCollection(name);
    }

}
