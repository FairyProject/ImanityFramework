package org.imanity.framework.mongo.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.NoArgsConstructor;
import org.imanity.framework.config.annotation.ConfigurationElement;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;

import java.nio.file.Path;

public abstract class YamlMongoConfiguration extends AbstractMongoConfiguration {

    private YamlImpl yaml;

    public abstract Path path();

    @Override
    public final String database() {
        this.ensureYamlLoaded();
        return yaml.DATABASE;
    }

    @Override
    protected void setupClientSettings(MongoClientSettings.Builder builder) {
        this.ensureYamlLoaded();
        builder.applyConnectionString(new ConnectionString(yaml.CONNECTION_STRING));
    }

    public final void ensureYamlLoaded() {
        if (yaml != null) {
            return;
        }

        yaml = new YamlImpl();
        yaml.loadAndSave();
    }

    private class YamlImpl extends YamlConfiguration {

        public String CONNECTION_STRING = "mongodb://localhost:27017";
        public String DATABASE = "database";

        public String COLLECTION_PREFIX = "imanity_";

        protected YamlImpl() {
            super(path(), YamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }
    }

}
