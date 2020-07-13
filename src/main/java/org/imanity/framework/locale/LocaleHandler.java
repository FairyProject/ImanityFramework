package org.imanity.framework.locale;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocaleHandler {

    private final Map<String, Locale> locales = new HashMap<>();

    public Locale registerLocale(String name) {
        Locale locale = new Locale();
        this.locales.put(name, locale);
        return locale;
    }

    public Locale registerFromYml(File file) {

        Locale locale = new Locale();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("locale");

        for (String key : config.getKeys(true)) {
            locale.registerEntry(key, config.getString(key));
        }

        this.locales.put(name, locale);

        return locale;

    }

    public void unregisterLocale(String name) {
        this.locales.remove(name);
    }

    public Locale getLocale(String name) {
        if (this.locales.containsKey(name)) {
            return this.locales.get(name);
        }

        return null;
    }

}
