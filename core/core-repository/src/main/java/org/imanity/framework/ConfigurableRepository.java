package org.imanity.framework;

import java.util.List;
import java.util.Optional;

public class ConfigurableRepository<T, ID> implements Repository<T, ID> {



    @Override
    public void init() {

    }

    @Override
    public Class<T> type() {
        return null;
    }

    @Override
    public <S extends T> S save(S pojo) {
        return null;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public <Q> Optional<T> findByQuery(String query, Q value) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public Iterable<T> findAllById(List<ID> ids) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ID id) {

    }

    @Override
    public <Q> void deleteByQuery(String query, Q value) {

    }

    @Override
    public void deleteAll() {

    }
}
