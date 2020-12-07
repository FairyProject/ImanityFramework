package org.imanity.framework.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.imanity.framework.Component;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.mongo.configuration.AbstractMongoConfiguration;

@Component
public class DefaultMongoConfiguration extends AbstractMongoConfiguration {
    @Override
    public String database() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MONGO.DATABASE;
    }

    @Override
    protected void setupClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString(ImanityCommon.CORE_CONFIG.STORAGE.MONGO.CONNECTION_STRING));
    }

    @Override
    public boolean shouldActivate() {
        return ImanityCommon.CORE_CONFIG.STORAGE.MONGO.ENABLED;
    }
}
