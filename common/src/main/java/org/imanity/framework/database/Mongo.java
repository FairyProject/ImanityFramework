/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.annotation.PostDestroy;
import org.imanity.framework.annotation.PostInitialize;
import org.imanity.framework.config.annotation.Comment;
import org.imanity.framework.config.annotation.ConfigurationElement;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.plugin.service.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

@Service(name = "mongo")
@Getter
public class Mongo {

    private MongoClient client;
    private MongoDatabase database;

    private MongoConfig config;

    public void generateConfig() {
        this.config = new MongoConfig();
        this.config.loadAndSave();
    }

    @PostInitialize
    public void init() {

        this.generateConfig();
        if (!ImanityCommon.CORE_CONFIG.isDatabaseTypeUsed(DatabaseType.MONGO)) {
            return;
        }

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

    @PostDestroy
    public void stop() {
        if (this.client != null) {
            this.client.close();
        }
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
