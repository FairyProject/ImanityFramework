package org.imanity.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import org.bson.BsonDocument;
import org.imanity.framework.mongo.MongoService;
import org.mongojack.JacksonMongoCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MongoRepository<T, ID extends Serializable> implements Repository<T, ID> {

    @Autowired
    private static MongoService MONGO_SERVICE;

    @Getter
    protected JacksonMongoCollection<T> collection;
    protected ObjectMapper objectMapper;

    @PostInitialize
    public void init() {
        this.collection = MONGO_SERVICE.collection(this.name(), this.getClass(), this.type(), this.objectMapper());

        this.postInit();
    }

    public ObjectMapper objectMapper() {
        return FrameworkMisc.JACKSON_MAPPER;
    }

    public abstract String name();

    public void postInit() {

    }

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
        return Optional.ofNullable(this.collection.findOneById(id));
    }

    @Override
    public <Q> Optional<T> findByQuery(String query, Q value) {
        return Optional.ofNullable(this.collection.findOne(Filters.eq(query, value)));
    }

    @Override
    public boolean existsById(ID id) {
        return this.findById(id).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        return this.collection.find();
    }

    @Override
    public Iterable<T> findAllById(List<ID> ids) {
        List<T> result = new ArrayList<>();
        for (T t : this.collection.find(this.collection.createIdInQuery(ids))) {
            result.add(t);
        }

        return result;
    }

    @Override
    public long count() {
        return this.collection.countDocuments();
    }

    @Override
    public void deleteById(ID id) {
        this.collection.removeById(id);
    }

    @Override
    public <Q> void deleteByQuery(String query, Q value) {
        this.collection.deleteMany(Filters.eq(query, value));
    }

    @Override
    public void deleteAll() {
        this.collection.deleteMany(new BsonDocument());
    }

    public String queryId() {
        return "_id";
    }

}
