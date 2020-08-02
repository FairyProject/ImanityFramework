package org.imanity.framework.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.annotation.Comment;
import org.imanity.framework.config.annotation.ConfigurationElement;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

@Getter
public class Mongo {

    private MongoClient client;
    private MongoDatabase database;

    private MongoConfig config;

    public void generateConfig() {
        this.config = new MongoConfig();
        this.config.loadAndSave();
    }

    public void init() {

        if (this.config.AUTHENTICATION.ENABLED) {

            MongoCredential credential = MongoCredential.createCredential(
                    this.config.AUTHENTICATION.USERNAME,
                    this.config.AUTHENTICATION.DATABASE,
                    this.config.AUTHENTICATION.PASSWORD.toCharArray()
            );

            this.client = new MongoClient(new ServerAddress(this.config.HOST, this.config.PORT), Collections.singletonList(credential));

        } else {
            this.client = new MongoClient(new ServerAddress(this.config.HOST, this.config.PORT));
        }

        this.database = this.client.getDatabase(this.config.DATABASE);
    }

    public static class MongoConfig extends YamlConfiguration {

        @Comment("host ip address")
        public String HOST = "localhost";

        @Comment({"host ip port", "must be greater than 1024"})
        public int PORT = 27017;

        public String DATABASE = "database";

        public String COLLECTION_PREFIX = "imanity_";

        public AuthCredentials AUTHENTICATION = new AuthCredentials();

        protected MongoConfig() {
            super(new File(ImanityCommon.BRIDGE.getDataFolder(), "mongo.yml").toPath(), YamlConfiguration.YamlProperties.builder()
                .setFormatter(FieldNameFormatters.LOWER_CASE)
                .setPrependedComments(Arrays.asList(
                        "================================",
                        "The configuration to adjust MongoDB Settings",
                        "================================",
                        " "
                )).build());
        }

        @ConfigurationElement
        @NoArgsConstructor
        public static class AuthCredentials {

            private boolean ENABLED = false;
            private String USERNAME = "user";
            private String PASSWORD = "password";
            private String DATABASE = "database";

        }
    }

}
