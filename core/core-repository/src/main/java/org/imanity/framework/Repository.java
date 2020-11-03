package org.imanity.framework;

import java.util.Optional;

public interface Repository<T, ID> {

    void init(RepositoryService repositoryService);

    Class<T> type();

    <S extends T> S save(S pojo);

    <S extends T> Iterable<S> saveAll(Iterable<S> pojoIterable);

    Optional<T> findById(ID id);

    Optional<T> findByExample(T example);

    <Q> Optional<T> findByQuery(String query, Q value);

    boolean existsById(ID id);

    Iterable<T> findAll();

    Iterable<T> findAllById(Iterable<ID> ids);

    long count();

    void deleteById(ID id);

    void delete(T t);

    void deleteAll(Iterable<? extends T> examples);

    void deleteAll();
}
