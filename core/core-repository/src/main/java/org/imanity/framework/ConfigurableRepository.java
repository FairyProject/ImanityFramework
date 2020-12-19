package org.imanity.framework;

import lombok.Getter;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.details.GenericBeanDetails;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class ConfigurableRepository<T, ID extends Serializable> implements Repository<T, ID> {

    private Repository<T, ID> repository;
    private BeanDetails serviceDetails;
    private boolean initialized;

    @PostInitialize
    public void init() {
        RepositoryType type = this.repositoryType();
        switch (type) {
            case MONGO:
                this.repository = new MongoRepository<T, ID>() {
                    @Override
                    public String name() {
                        return ConfigurableRepository.this.name();
                    }

                    @Override
                    public Class<T> type() {
                        return ConfigurableRepository.this.type();
                    }
                };
                break;
            default:
                this.repository = new SQLRepository<T, ID>(type) {

                    @Override
                    public Class<T> type() {
                        return ConfigurableRepository.this.type();
                    }
                };
                break;
        }

        this.serviceDetails = new GenericBeanDetails(this.repository);

        try {
            this.serviceDetails.call(PostInitialize.class);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something wrong while calling PostInitialize for ConfigurableRepository", e);
        }
        this.initialized = true;
    }

    @PreDestroy
    public void preClose() {
        try {
            this.serviceDetails.call(PreDestroy.class);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something wrong while calling PreDestroy for ConfigurableRepository", e);
        }
    }

    @PostDestroy
    public void postClose() {
        try {
            this.serviceDetails.call(PostDestroy.class);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something wrong while calling PostDestroy for ConfigurableRepository", e);
        }
    }

    public abstract RepositoryType repositoryType();

    public abstract String name();

    @Override
    public <S extends T> S save(S pojo) {
        return this.repository.save(pojo);
    }

    @Override
    public Optional<T> findById(ID id) {
        return this.repository.findById(id);
    }

    @Override
    public <Q> Optional<T> findByQuery(String query, Q value) {
        return this.repository.findByQuery(query, value);
    }

    @Override
    public boolean existsById(ID id) {
        return this.repository.existsById(id);
    }

    @Override
    public Iterable<T> findAll() {
        return this.repository.findAll();
    }

    @Override
    public Iterable<T> findAllById(List<ID> ids) {
        return this.repository.findAllById(ids);
    }

    @Override
    public long count() {
        return this.repository.count();
    }

    @Override
    public void deleteById(ID id) {
        this.repository.deleteById(id);
    }

    @Override
    public <Q> void deleteByQuery(String query, Q value) {
        this.repository.deleteByQuery(query, value);
    }

    @Override
    public void deleteAll() {
        this.repository.deleteAll();
    }
}
