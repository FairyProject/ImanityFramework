package org.imanity.framework.locale.type;

import me.skymc.taboolib.mysql.builder.SQLColumnType;
import org.imanity.framework.Imanity;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.player.data.type.impl.AbstractData;

public class LocaleTypeData extends AbstractData<Locale> {

    private Locale locale;

    @Override
    public Locale get() {
        return locale;
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
    public SQLColumnType columnType() {
        return SQLColumnType.TINYTEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + locale.getName() + (sql ? "\"" : "");
    }
}
