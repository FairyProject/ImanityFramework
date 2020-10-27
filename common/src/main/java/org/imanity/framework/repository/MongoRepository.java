package org.imanity.framework.repository;

import java.util.Optional;

public class MongoRepository<T, ID> implements Repository<T, ID> {
    @Override
    public <S extends T> S save(S var1) {
        return null;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> var1) {
        return null;
    }

    @Override
    public Optional<T> findById(ID var1) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ID var1) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> var1) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ID var1) {

    }

    @Override
    public void delete(T var1) {

    }

    @Override
    public void deleteAll(Iterable<? extends T> var1) {

    }

    @Override
    public void deleteAll() {

    }
}
