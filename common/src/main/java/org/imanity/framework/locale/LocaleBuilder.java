package org.imanity.framework.locale;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.util.CommonUtility;
import org.imanity.framework.util.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class LocaleBuilder {

    private String name;
    private List<Entry<String, String>> entries = new ArrayList<>();

    public LocaleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public LocaleBuilder entry(String key, String value) {
        Entry<String, String> entry = new Entry<>(key, value);
        this.entries.add(entry);
        return this;
    }

    public LocaleBuilder entry(String key, Iterable<String> value) {
        return this.entry(key, CommonUtility.joinToString(value, "\n"));
    }

    public LocaleBuilder entry(String key, String[] value) {
        return this.entry(key, CommonUtility.joinToString(value, "\n"));
    }

    public LocaleBuilder entries(String... entries) {

        if (entries.length % 2 != 0) {
            throw new IllegalStateException("The entries is not even-numbered!");
        }

        for (int i = 0; i < entries.length; i += 2) {
            this.entry(entries[i], entries[i + 1]);
        }

        return this;

    }

    public Locale build() {
        Locale locale = ImanityCommon.LOCALE_HANDLER.getOrRegister(name);
        for (Entry<String, String> entry : this.entries) {
            locale.registerEntry(entry.getKey(), entry.getValue());
        }
        return locale;
    }

}
