package org.imanity.framework.locale;

import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.imanity.framework.ObjectSerializer;

@Component
public class LocaleSerializer implements ObjectSerializer<Locale, String> {

    @Autowired
    private LocaleHandler localeHandler;

    @Override
    public String serialize(Locale input) {
        return input.getName();
    }

    @Override
    public Locale deserialize(String output) {
        return this.localeHandler.getLocale(output);
    }

    @Override
    public Class<Locale> inputClass() {
        return Locale.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
