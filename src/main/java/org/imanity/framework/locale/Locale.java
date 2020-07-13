package org.imanity.framework.locale;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class Locale {

    private final Char2ObjectOpenHashMap<Map<String, String>> translateEntries = new Char2ObjectOpenHashMap<>();

    protected Locale() {

    }

    public void registerEntry(String key, String value) {
        char c = this.getEntry(key);

        Map<String, String> subEntries;
        if (this.translateEntries.containsKey(c)) {
            subEntries = this.translateEntries.get(c);
        } else {
            subEntries = new Object2ObjectOpenHashMap<>();
            this.translateEntries.put(c, subEntries);
        }

        subEntries.put(key, value);
    }

    public void unregisterEntry(String key) {
        char c = this.getEntry(key);

        if (translateEntries.containsKey(c)) {
            Map<String, String> subEntries = this.translateEntries.get(c);

            subEntries.remove(key);

            if (subEntries.isEmpty()) {
                translateEntries.remove(c);
            }
        }
    }

    public String get(String key) {
        char c = this.getEntry(key);

        if (this.translateEntries.containsKey(c)) {
            Map<String, String> subEntries = this.translateEntries.get(c);

            return subEntries.get(key);
        }

        return null;
    }

    public char getEntry(String key) {
        return key.charAt(0);
    }
}
