package org.imanity.framework.locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.util.Utility;

import java.io.IOException;
import java.util.Map;

@JsonSerialize(using = Locale.Serializer.class)
@JsonDeserialize(using = Locale.Deserializer.class)
public class Locale {

    @Autowired
    public static LocaleHandler LOCALE_HANDLER;

    private final Char2ObjectOpenHashMap<Map<String, String>> translateEntries = new Char2ObjectOpenHashMap<>();

    @Getter
    private final String name;

    protected Locale(String name) {
        this.name = name;
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

    public void registerEntry(String key, Iterable<String> strings) {
        this.registerEntry(key, Utility.joinToString(strings, "\n"));
    }

    public void registerEntry(String key, String[] strings) {
        this.registerEntry(key, Utility.joinToString(strings, "\n"));
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

            if (subEntries.containsKey(key)) {
                return subEntries.get(key);
            }
            return key;
        }

        return key;
    }

    public char getEntry(String key) {
        return key.charAt(0);
    }

    public static class Serializer extends StdSerializer<Locale> {


        protected Serializer() {
            super(Locale.class);
        }

        @Override
        public void serialize(Locale locale, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(locale.name);
        }

    }

    public static class Deserializer extends StdDeserializer<Locale> {

        protected Deserializer() {
            super(Locale.class);
        }

        @Override
        public Locale deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return LOCALE_HANDLER.getOrRegister(jsonParser.getValueAsString());
        }
    }
}
