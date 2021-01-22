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

package org.imanity.framework.mysql;

import lombok.SneakyThrows;
import org.imanity.framework.*;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.AbstractConnectionFactory;
import org.jetbrains.annotations.Nullable;

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

    private Map<Class<?>, AbstractConnectionFactory> connectionFactories;
    private Class<?> defaultConfiguration;

    @Autowired
    private SerializerFactory serializerFactory;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;

        this.connectionFactories = new ConcurrentHashMap<>();

        FrameworkMisc.LIBRARY_HANDLER.downloadLibraries(Library.BYTE_BUDDY);
        FrameworkMisc.LIBRARY_HANDLER.obtainClassLoaderWith(Library.BYTE_BUDDY);

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { AbstractSqlConfiguration.class };
            }

            @Override
            @SneakyThrows
            public void onEnable(Object instance) {
                AbstractSqlConfiguration configuration = (AbstractSqlConfiguration) instance;

                if (!configuration.shouldActivate()) {
                    return;
                }

                if (defaultConfiguration == null) {
                    defaultConfiguration = configuration.getClass();
                }

                AbstractConnectionFactory factory = configuration.factory();

                factory.connect();
                connectionFactories.put(configuration.getClass(), factory);
            }

            @Override
            public void onDisable(Object instance) {
                shutdownFactory(instance.getClass());
            }
        });
    }

    public ObjectSerializer<?, ?> findSerializer(Class<?> type) {
        return this.serializerFactory.findSerializer(type);
    }

    @PreDestroy
    @SneakyThrows
    public void close() {
        for (Class<?> configuration : this.connectionFactories.keySet()) {
            this.shutdownFactory(configuration);
        }
    }

    public void shutdownFactory(Class<?> configuration) {
        AbstractConnectionFactory connectionFactory = connectionFactories.get(configuration);

        if (connectionFactory != null) {
            try {
                connectionFactory.shutdown();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.connectionFactories.remove(configuration);
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

        if (this.connectionFactories == null) {
            throw new IllegalArgumentException("SQLService haven't been loaded!");
        }

        AbstractConnectionFactory factory = this.connectionFactories.getOrDefault(type, null);
        if (factory == null) {
            throw new IllegalArgumentException("The database hasn't registered!");
        }

        return factory;
    }

}
