package org.imanity.framework.config.converters;

import org.imanity.framework.config.util.Converter;
import org.imanity.framework.database.DatabaseType;

public class DatabaseTypeConverter implements Converter<DatabaseType, String> {

    @Override
    public DatabaseType convertFrom(String element, ConversionInfo info) {
        return DatabaseType.valueOf(element.toUpperCase());
    }

    @Override
    public String convertTo(DatabaseType element, ConversionInfo info) {
        return element.name();
    }
}
