package org.imanity.framework.locale.type;

import org.imanity.framework.util.builder.SQLColumnType;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.data.type.impl.AbstractDataConverter;

import java.lang.reflect.Field;

public class LocaleTypeDataConverter extends AbstractDataConverter<Locale> {

    private Locale locale;

    @Override
    public String get() {
        return locale.getName();
    }

    @Override
    public void set(Object object) {
        if (object instanceof Locale) {
            this.locale = (Locale) object;
        }
        if (object instanceof String) {
            this.locale = ImanityCommon.LOCALE_HANDLER.getLocale((String) object);
            if (this.locale == null) {
                this.locale = ImanityCommon.LOCALE_HANDLER.getOrRegister((String) object);
            }
        }
    }

    @Override
    public Object toFieldObject(Field field) {
        return locale;
    }

    @Override
    public Class<?> getLoadType() {
        return String.class;
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.TINYTEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + locale.getName() + (sql ? "\"" : "");
    }
}
