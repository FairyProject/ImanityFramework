package org.imanity.framework.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonChain
{
    private final JsonObject json;

    public JsonChain() {
        this.json = new JsonObject();
    }

    public JsonChain addProperty(final String property, final String value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Object value) {
        this.json.addProperty(property, value.toString());
        return this;
    }

    public JsonChain addProperty(final String property, final Number value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Boolean value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain addProperty(final String property, final Character value) {
        this.json.addProperty(property, value);
        return this;
    }

    public JsonChain add(final String property, final JsonElement element) {
        this.json.add(property, element);
        return this;
    }

    public JsonObject get() {
        return this.json;
    }
}
