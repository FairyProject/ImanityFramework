package org.imanity.framework.jongo.configuration;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public abstract class AbstractMongoConfiguration {

    public abstract String database();

    public abstract ServerAddress serverAddress();

    public abstract MongoCredential credential();

    public MongoClientOptions mongoClientOptions() {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        this.setupClientOptions(builder);
        return builder.build();
    }

    protected void setupClientOptions(MongoClientOptions.Builder builder) {

    }

}
