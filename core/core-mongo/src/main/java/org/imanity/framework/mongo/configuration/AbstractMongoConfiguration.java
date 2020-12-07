package org.imanity.framework.mongo.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;

public abstract class AbstractMongoConfiguration {

    public abstract String database();

    public boolean shouldActivate() {
        return true;
    }

    public MongoClientSettings mongoClientSettings() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.JAVA_LEGACY);
        this.setupClientSettings(builder);
        return builder.build();
    }

    protected void setupClientSettings(MongoClientSettings.Builder builder) {

    }

}
