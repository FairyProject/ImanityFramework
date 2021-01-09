package org.imanity.framework;

import org.imanity.framework.mysql.SqlService;
import org.imanity.framework.mysql.Session;
import org.imanity.framework.mysql.connection.AbstractConnectionFactory;
import org.imanity.framework.mysql.pojo.Transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class SQLRepository<T, ID extends Serializable> implements Repository<T, ID> {

    private RepositoryType type;
    private AbstractConnectionFactory factory;

    public SQLRepository() {
    }


    public SQLRepository(RepositoryType type) {
        this.type = type;
    }

    @PostInitialize
    public void init() {
        if (this.type != null) {
            this.factory = SqlService.INSTANCE.factory(this.getClass(), type);
        } else {
            this.factory = SqlService.INSTANCE.factory(this.getClass(), null);
        }

        this.factory.createTable(this.type());
    }

    public <T> T performSessionResult(Function<Session, T> sessionConsumer) {
        if (this.factory == null) {
            throw new IllegalArgumentException("Attempt to perform action before repository initialized!");
        }

        T result = null;

        Transaction transaction = null;
        try {
            transaction = this.factory.startTransaction();

            Session session = this.factory.session(transaction);
            result = sessionConsumer.apply(session);

            transaction.commit();
        } catch (Throwable throwable) {
            if (transaction != null) {
                transaction.rollback();
            }
            throwable.printStackTrace();
            return null;
        }

        return result;
    }

    public void performSession(Consumer<Session> sessionConsumer) {
        Transaction transaction = null;
        try {
            transaction = this.factory.startTransaction();

            Session session = this.factory.session(transaction);
            sessionConsumer.accept(session);

            transaction.commit();
        } catch (Throwable throwable) {
            if (transaction != null) {
                transaction.rollback();
            }
            throwable.printStackTrace();
        }
    }

    @Override
    public <S extends T> S save(S pojo) {
        this.performSession(session -> session.upsert(pojo));
        return pojo;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(this.performSessionResult(session -> session.find(this.type(), id)));
    }

    @Override
    public <Q> Optional<T> findByQuery(String queryName, Q value) {
        return Optional.ofNullable(this.performSessionResult(session -> session.findByQuery(this.type(), queryName, value)));
    }

    @Override
    public boolean existsById(ID id) {
        return this.performSessionResult(session -> session.find(this.type(), id) != null);
    }

    @Override
    public Iterable<T> findAll() {
        return this.performSessionResult(session -> session.results(this.type()));
    }

    @Override
    public Iterable<T> findAllById(List<ID> ids) {
        return this.performSessionResult(session -> session.query().byMultipleIds(this.type(), ids).results(this.type()));
    }

    @Override
    public long count() {
        return this.performSessionResult(session -> session.query().count(this.type()).first(Long.class));
    }

    @Override
    public void deleteById(ID id) {
        this.performSession(session -> session.delete(id));
    }

    @Override
    public <Q> void deleteByQuery(String queryName, Q value) {
        this.performSession(session -> session.query()
                .whereQuery(queryName, value)
                .delete());
    }

    @Override
    public void deleteAll() {
        this.performSession(session -> session.query().delete());
    }


}
