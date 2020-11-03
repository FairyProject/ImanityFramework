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

package org.imanity.framework.config;

import org.imanity.framework.config.annotation.Convert;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.config.converters.DatabaseTypeConverter;
import org.imanity.framework.config.format.FieldNameFormatters;
import org.imanity.framework.config.yaml.YamlConfiguration;
import org.imanity.framework.database.DatabaseType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CoreConfig extends YamlConfiguration {

    @Convert(DatabaseTypeConverter.class)
    private DatabaseType DEFAULT_DATABASE = DatabaseType.FLAT_FILE;

    @Convert(SpecificDatabasesConverter.class)
    private Map<String, DatabaseType> SPECIFIC_DATABASES = new HashMap<>();

    public boolean USE_REDIS = false;

    public boolean USE_REDIS_DISTRIBUTED_LOCK = false;

    public boolean USE_LOCALE = false;

    public boolean ASYNCHRONOUS_DATA_STORING = true;

    public String CURRENT_SERVER = "server-1";

    public String DEFAULT_LOCALE = "en_us";

    public CoreConfig() {
        super(new File(ImanityCommon.BRIDGE.getDataFolder(), "core.yml").toPath(), YamlProperties
            .builder()
                .setFormatter(FieldNameFormatters.LOWER_CASE)
            .setPrependedComments(Arrays.asList(
                    "==============================",
                    "The configuration to adjust data settings",
                    "==============================",
                    " "
            )).build());
    }

    public DatabaseType getDatabaseType(String name) {
        if (SPECIFIC_DATABASES.containsKey(name)) {
            return SPECIFIC_DATABASES.get(name);
        }
        return DEFAULT_DATABASE;
    }

    public boolean isDatabaseTypeUsed(DatabaseType databaseType) {
        if (DEFAULT_DATABASE == databaseType) {
            return true;
        }
        return SPECIFIC_DATABASES.containsValue(databaseType);
    }

    private static class SpecificDatabasesConverter implements Converter<Map<String, DatabaseType>, List<String>> {

        @Override
        public List<String> convertTo(Map<String, DatabaseType> element, ConversionInfo info) {
            return element.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue().name())
                    .collect(Collectors.toList());
        }

        @Override
        public Map<String, DatabaseType> convertFrom(List<String> element, ConversionInfo info) {
            return element.stream()
                    .map(s -> s.split(":"))
                    .collect(Collectors.toMap(s -> s[0], s -> DatabaseType.valueOf(s[1].toUpperCase())));
        }
    }
}
