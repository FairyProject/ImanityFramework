/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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
