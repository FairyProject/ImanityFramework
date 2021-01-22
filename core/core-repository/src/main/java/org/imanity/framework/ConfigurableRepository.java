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
