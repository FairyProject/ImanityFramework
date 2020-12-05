package org.imanity.framework.mysql;

import lombok.SneakyThrows;
import org.imanity.framework.*;
import org.imanity.framework.mysql.config.AbstractSqlConfiguration;
import org.imanity.framework.mysql.connection.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "sql")
public class SQLService {

    private List<AbstractSqlConfiguration<?>> preConfigurations;
    private Map<Class<?>, ConnectionFactory> connectionFactories;
    private Class<?> defaultConfiguration;

    @PreInitialize
    public void preInit() {
        this.preConfigurations = new ArrayList<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { AbstractSqlConfiguration.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                AbstractSqlConfiguration<?> configuration = (AbstractSqlConfiguration<?>) super.newInstance(type);
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

            ConnectionFactory factory = configuration.factory();
            this.connectionFactories.put(configuration.getClass(), factory);
        }
        this.preConfigurations.clear();
        this.preConfigurations = null;
    }

}
