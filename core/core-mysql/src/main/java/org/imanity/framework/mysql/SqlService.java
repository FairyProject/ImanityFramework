package org.imanity.framework.mysql;

import lombok.SneakyThrows;
import org.imanity.framework.*;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.AbstractConnectionFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * ORM original by norm
 *
 */
@Service(name = "sql", dependencies = "serializer")
public class SqlService {

    public static SqlService INSTANCE;

    private List<AbstractSqlConfiguration<?>> preConfigurations;
    private Map<Class<?>, AbstractConnectionFactory> connectionFactories;
    private Class<?> defaultConfiguration;

    @Autowired
    private SerializerFactory serializerFactory;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;

        this.preConfigurations = new ArrayList<>();
        FrameworkMisc.LIBRARY_HANDLER.downloadLibraries(Library.BYTE_BUDDY);
        FrameworkMisc.LIBRARY_HANDLER.obtainClassLoaderWith(Library.BYTE_BUDDY);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { AbstractSqlConfiguration.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                AbstractSqlConfiguration<?> configuration = (AbstractSqlConfiguration<?>) super.newInstance(type);
                if (!configuration.shouldActivate()) {
                    return null;
                }
                preConfigurations.add(configuration);
                return configuration;
            }
        });
    }

    @SneakyThrows
    @PostInitialize
    public void init() {
        this.connectionFactories = new ConcurrentHashMap<>(this.preConfigurations.size());

        for (AbstractSqlConfiguration<?> configuration : this.preConfigurations) {
            if (this.defaultConfiguration == null) {
                this.defaultConfiguration = configuration.getClass();
            }

            AbstractConnectionFactory factory = configuration.factory();

            factory.connect();
            this.connectionFactories.put(configuration.getClass(), factory);
        }
        this.preConfigurations.clear();
        this.preConfigurations = null;
    }

    public ObjectSerializer<?, ?> findSerializer(Class<?> type) {
        return this.serializerFactory.findSerializer(type);
    }

    @PreDestroy
    @SneakyThrows
    public void close() {
        for (AbstractConnectionFactory factory : this.connectionFactories.values()) {
            factory.shutdown();
        }
    }

    public AbstractConnectionFactory factory(Class<?> use, @Nullable RepositoryType repositoryType) {
        Class<?> type;
        ProvideConfiguration configuration = use.getAnnotation(ProvideConfiguration.class);
        if (configuration != null) {
            type = configuration.value();
        } else {
            if (repositoryType != null) {
                for (AbstractConnectionFactory factory : this.connectionFactories.values()) {
                    if (factory.type() == repositoryType) {
                        return factory;
                    }
                }

                throw new IllegalArgumentException("There is no sql configuration with specified type " + repositoryType.name() + " registered!");
            }

            type = this.defaultConfiguration;

            if (type == null) {
                throw new IllegalArgumentException("There is no sql configuration registered!");
            }
        }

        if (!AbstractSqlConfiguration.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("The type " + type.getSimpleName() + " wasn't implemented on AbstractMongoConfiguration!");
        }

        AbstractConnectionFactory factory = this.connectionFactories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("The database hasn't registered");
        }

        return factory;
    }

}
