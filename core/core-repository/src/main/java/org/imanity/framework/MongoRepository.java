package org.imanity.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonParser;
import lombok.Getter;
import org.bson.BsonDocument;
import org.bson.Document;
import org.imanity.framework.mongo.MongoService;
import org.mongojack.JacksonMongoCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ServiceDependency(dependencies = { "mongo" })
public abstract class MongoRepository<T, ID> implements Repository<T, ID> {

    private static final BsonFactory BSON_FACTORY;

    @Autowired
    private static MongoService MONGO_SERVICE;

    static {
        BSON_FACTORY = new BsonFactory();
        BSON_FACTORY.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
    }

    @Getter
    protected JacksonMongoCollection<T> collection;
    protected ObjectMapper objectMapper;

    @PostInitialize
    public void init() {
        this.collection = MONGO_SERVICE.collection(this.name(), this.getClass(), this.type());

        this.objectMapper = new ObjectMapper(BSON_FACTORY);
        this.configureJacksonMapper(this.objectMapper);
    }

    public void configureJacksonMapper(ObjectMapper objectMapper) {

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
        return Optional.ofNullable(this.collection.findOneById(id));
    }

    @Override
    public Optional<T> findByExample(T example) {
        try {
            return Optional.ofNullable(this.collection.findOne(this.toBson(example)));
        } catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }
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
    public void delete(T example) {
        this.collection.deleteMany(this.toBson(example));
    }

    @Override
    public void deleteAll(Iterable<? extends T> examples) {
        examples.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        this.collection.deleteMany(new BsonDocument());
    }

    private Document toBson(Object example) {
        try {
            byte[] bytes = FrameworkMisc.JACKSON_MAPPER.writer().writeValueAsBytes(example);

            return BSON_FACTORY.createJsonParser(bytes).readValueAs(Document.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
