package org.imanity.framework.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.imanity.framework.Imanity;
import org.imanity.framework.config.util.annotation.Comment;
import org.imanity.framework.config.util.annotation.ConfigurationElement;
import org.imanity.framework.config.util.yaml.BukkitYamlConfiguration;
import org.imanity.framework.config.util.format.FieldNameFormatters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

@Getter
public class Mongo {

    private MongoClient client;
    private MongoDatabase database;

    private MongoConfig config;

    public void init() {
        MongoConfig mongoConfig = new MongoConfig();
        mongoConfig.loadAndSave();

        this.config = mongoConfig;

        if (mongoConfig.AUTHENTICATION.ENABLED) {

            MongoCredential credential = MongoCredential.createCredential(
                    mongoConfig.AUTHENTICATION.USERNAME,
                    mongoConfig.AUTHENTICATION.DATABASE,
                    mongoConfig.AUTHENTICATION.PASSWORD.toCharArray()
            );

            this.client = new MongoClient(new ServerAddress(mongoConfig.HOST, mongoConfig.PORT), Collections.singletonList(credential));

        } else {
            this.client = new MongoClient(new ServerAddress(mongoConfig.HOST, mongoConfig.PORT));
        }

        this.database = this.client.getDatabase(mongoConfig.DATABASE);
    }

    public static class MongoConfig extends BukkitYamlConfiguration {

        @Comment("host ip address")
        public String HOST = "localhost";

        @Comment({"host ip port", "must be greater than 1024"})
        public int PORT = 27017;

        public String DATABASE = "database";

        public String COLLECTION_PREFIX = "imanity_";

        public AuthCredentials AUTHENTICATION = new AuthCredentials();

        protected MongoConfig() {
            super(new File(Imanity.PLUGIN.getDataFolder(), "mongo.yml").toPath(), BukkitYamlProperties.builder()
                .setFormatter(FieldNameFormatters.LOWER_UNDERSCORE)
                .setPrependedComments(Arrays.asList(
                        "================================",
                        "The configuration to adjust MongoDB Settings",
                        "================================"
                )).build());
        }

        @ConfigurationElement
        class AuthCredentials {

            private boolean ENABLED = false;
            private String USERNAME = "user";
            private String PASSWORD = "password";
            private String DATABASE = "database";

        }
    }

}
