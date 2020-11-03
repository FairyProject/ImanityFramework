package org.imanity.framework;

import lombok.Getter;
import org.bson.types.ObjectId;
import org.imanity.framework.jongo.Jongo;
import org.imanity.framework.jongo.MongoCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.imanity.framework.jongo.Oid.withOid;

public abstract class MongoRepository<T, ID> implements Repository<T, ID> {

    @Getter
    protected MongoCollection collection;

    @Override
    public void init(RepositoryService repositoryService) {
        this.collection = repositoryService.getMongoService().collection(this.name(), this.getClass());
    }

    public abstract String name();

    @Override
    public <S extends T> S save(S pojo) {
        this.collection.save(pojo);
        return pojo;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> pojoIterable) {
        pojoIterable.forEach(pojo -> this.collection.save(pojo));
        return pojoIterable;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(this.collection.findOne(withOid(id.toString())).as(this.type()));
    }

    @Override
    public Optional<T> findByExample(T example) {
        return Optional.ofNullable(this.collection.findOne(example).as(this.type()));
    }

    @Override
    public <Q> Optional<T> findByQuery(String query, Q value) {
        return Optional.ofNullable(this.collection.findOne("{" + query + ": #}", value).as(this.type()));
    }

    @Override
    public boolean existsById(ID id) {
        return this.collection.findOne(withOid(id.toString())).as(this.type()) != null;
    }

    @Override
    public Iterable<T> findAll() {
        return this.collection.find().as(this.type());
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            for (T t : this.collection.find("{ _id: # }", new ObjectId(id.toString())).as(this.type())) {
                result.add(t);
            }
        }

        return result;
    }

    @Override
    public long count() {
        return this.collection.count();
    }

    @Override
    public void deleteById(ID id) {
        this.collection.remove(new ObjectId(id.toString()));
    }

    @Override
    public void delete(T t) {
        this.collection.remove(t);
    }

    @Override
    public void deleteAll(Iterable<? extends T> examples) {
        examples.forEach(this.collection::remove);
    }

    @Override
    public void deleteAll() {
        this.collection.remove();
    }
}
