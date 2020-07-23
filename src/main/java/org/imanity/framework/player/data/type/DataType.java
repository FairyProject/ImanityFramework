package org.imanity.framework.player.data.type;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.bson.Document;
import org.imanity.framework.player.data.type.impl.*;

import java.util.HashMap;
import java.util.Map;

public class DataType {

    private static final Map<Class<?>, DataType> DATA_TYPES = new HashMap<>();

    public static final DataType JSON;
    public static final DataType STRING;
    public static final DataType INT;
    public static final DataType DOUBLE;
    public static final DataType BOOLEAN;
    public static final DataType FLOAT;
    public static final DataType SHORT;
    public static final DataType DOCUMENT;

    static {
        JSON = DataType.register(JsonObject.class, JsonData.class);
        STRING = DataType.register(String.class, StringData.class);

        DataType.register(int.class, IntData.class);
        DataType.register(short.class, IntData.class);
        DataType.register(double.class, DoubleData.class);
        DataType.register(float.class, DoubleData.class);
        DataType.register(boolean.class, BooleanData.class);

        INT = DataType.register(Integer.class, IntData.class);
        SHORT = DataType.register(Short.class, IntData.class);
        DOUBLE = DataType.register(Double.class, DoubleData.class);
        FLOAT = DataType.register(Float.class, DoubleData.class);
        BOOLEAN = DataType.register(Boolean.class, BooleanData.class);
        DOCUMENT = DataType.register(Document.class, DocumentData.class);
    }

    @Getter
    private final Class<? extends Data<?>> dataClass;

    public DataType(Class<? extends Data<?>> dataClass) {
        this.dataClass = dataClass;
    }

    public Data<?> newData() {
        try {
            return dataClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DataType register(Class<?> clazz, Class<? extends Data<?>> dataClass) {
        DataType dataType = new DataType(dataClass);
        DATA_TYPES.put(clazz, dataType);
        return dataType;
    }

    public static DataType getType(Class<?> clazz) {

        return DATA_TYPES.get(clazz);

    }

}
