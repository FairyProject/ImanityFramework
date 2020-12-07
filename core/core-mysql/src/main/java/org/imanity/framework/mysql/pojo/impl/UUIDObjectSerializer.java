package org.imanity.framework.mysql.pojo.impl;

import org.imanity.framework.Component;
import org.imanity.framework.ObjectSerializer;

import java.util.UUID;

@Component
public class UUIDObjectSerializer implements ObjectSerializer<UUID, String> {
    @Override
    public String serialize(UUID input) {
        return input.toString();
    }

    @Override
    public UUID deserialize(String output) {
        return UUID.fromString(output);
    }

    @Override
    public Class<UUID> inputClass() {
        return UUID.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
