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
