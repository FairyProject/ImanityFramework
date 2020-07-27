package org.imanity.framework.locale.type;

import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.imanity.framework.Imanity;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.player.data.type.impl.AbstractData;

import java.lang.reflect.Field;

public class LocaleTypeData extends AbstractData<Locale> {

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
            this.locale = Imanity.LOCALE_HANDLER.getLocale((String) object);
            if (this.locale == null) {
                this.locale = Imanity.LOCALE_HANDLER.registerLocale((String) object);
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
