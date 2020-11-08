package org.imanity.framework;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    void init();

    Class<T> type();

    <S extends T> S save(S pojo);

    default <S extends T> Iterable<S> saveAll(Iterable<S> pojoIterable) {
        pojoIterable.forEach(this::save);
        return pojoIterable;
    }

    Optional<T> findById(ID id);

    <Q> Optional<T> findByQuery(String query, Q value);

    boolean existsById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(List<ID> ids);

    long count();

    void deleteById(ID id);

    <Q> void deleteByQuery(String query, Q value);

    void deleteAll();
}
