package org.imanity.framework.mongo.configuration;

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
    public final ServerAddress serverAddress() {
        this.ensureYamlLoaded();
        return new ServerAddress(yaml.IP_ADDRESS, yaml.PORT);
    }

    @Override
    public final MongoCredential credential() {
        this.ensureYamlLoaded();
        return this.yaml.AUTHENTICATION.ENABLED ? MongoCredential.createCredential(this.yaml.AUTHENTICATION.USERNAME, this.yaml.AUTHENTICATION.DATABASE, this.yaml.AUTHENTICATION.PASSWORD.toCharArray()) : null;
    }

    @Override
    public final String database() {
        this.ensureYamlLoaded();
        return yaml.DATABASE;
    }

    public final void ensureYamlLoaded() {
        if (yaml != null) {
            return;
        }

        yaml = new YamlImpl();
        yaml.loadAndSave();
    }

    private class YamlImpl extends YamlConfiguration {

        public String IP_ADDRESS = "localhost";
        public int PORT = 27017;
        public String DATABASE = "database";

        public String COLLECTION_PREFIX = "imanity_";

        public AuthCredentials AUTHENTICATION = new AuthCredentials();

        protected YamlImpl() {
            super(path(), YamlProperties.builder()
                    .setFormatter(FieldNameFormatters.LOWER_CASE)
                    .build());
        }

        @ConfigurationElement
        @NoArgsConstructor
        private class AuthCredentials {

            private boolean ENABLED = false;
            private String USERNAME = "user";
            private String PASSWORD = "password";
            private String DATABASE = "database";

        }
    }

}
