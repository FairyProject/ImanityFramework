package org.imanity.framework;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "serializer")
@Getter
public class SerializerFactory {

    private Map<Class<?>, ObjectSerializer> serializers;

    @PreInitialize
    public void preInit() {
        this.serializers = new ConcurrentHashMap<>();

        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { ObjectSerializer.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                ObjectSerializer serializer = (ObjectSerializer) super.newInstance(type);

                if (serializers.containsKey(serializer.inputClass())) {
                    throw new IllegalArgumentException("The Serializer for " + serializer.inputClass().getName() + " already exists!");
                }

                serializers.put(serializer.inputClass(), serializer);
                return serializer;
            }
        });
    }

    @Nullable
    public ObjectSerializer findSerializer(Class<?> type) {
        return this.serializers.getOrDefault(type, null);
    }

    @PostInitialize
    public void postInit() {
        SimpleModule module = new SimpleModule();

        for (ObjectSerializer serializer : this.serializers.values()) {
            module.addSerializer(new JacksonSerailizer(serializer));
            module.addDeserializer(serializer.inputClass(), new JacksonDeserailizer(serializer));
        }

        FrameworkMisc.JACKSON_MAPPER.registerModule(module);
    }

    public static class JacksonSerailizer extends StdSerializer {

        private final ObjectSerializer serializer;

        public JacksonSerailizer(ObjectSerializer serializer) {
            super(serializer.inputClass());

            this.serializer = serializer;
        }

        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
           jsonGenerator.writeObject(serializer.serialize(o));
        }
    }

    public static class JacksonDeserailizer extends StdDeserializer {

        private final ObjectSerializer serializer;

        public JacksonDeserailizer(ObjectSerializer serializer) {
            super(serializer.inputClass());

            this.serializer = serializer;
        }


        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return serializer.deserialize(jsonParser.readValueAs(serializer.outputClass()));
        }
    }

}
