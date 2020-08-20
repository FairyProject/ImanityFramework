package org.imanity.framework.data.type;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.bson.Document;
import org.imanity.framework.data.type.impl.*;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

public class DataConverterType {

    private static final Map<Class<?>, Function<Field, DataConverterType>> DATA_TYPES = new HashMap<>();

    public static final DataConverterType JSON;
    public static final DataConverterType STRING;
    public static final DataConverterType INT;
    public static final DataConverterType DOUBLE;
    public static final DataConverterType BOOLEAN;
    public static final DataConverterType FLOAT;
    public static final DataConverterType SHORT;
    public static final DataConverterType DOCUMENT;
    public static final DataConverterType ENTRY;
    public static final DataConverterType UUID;

    static {
        JSON = DataConverterType.register(JsonDataConverter.class, JsonObject.class);
        STRING = DataConverterType.register(StringDataConverter.class, String.class);
        ENTRY = DataConverterType.register(EntryDataConverter.class, Entry.class);
        UUID = DataConverterType.register(UUIDDataConverter.class, java.util.UUID.class);

        INT = SHORT = DataConverterType.register(IntDataConverter.class,
                int.class,
                short.class,
                Integer.class,
                Short.class
        );
        FLOAT = DOUBLE = DataConverterType.register(DoubleDataConverter.class,
                double.class,
                float.class,
                Double.class,
                Float.class
        );
        BOOLEAN = DataConverterType.register(BooleanDataConverter.class,
                boolean.class,
                Boolean.class
        );

        DOCUMENT = DataConverterType.register(DocumentDataConverter.class, Document.class);

        DataConverterType.register(ListDataConverterType::new,
                List.class,
                ArrayList.class,
                ObjectArrayList.class,
                EntryArrayList.class
                );
    }

    @Getter
    private final Class<? extends DataConverter<?>> dataClass;

    public DataConverterType(Class<? extends DataConverter<?>> dataClass) {
        this.dataClass = dataClass;
    }

    public DataConverter<?> newData() {
        try {
            return dataClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DataConverterType register(Class<? extends DataConverter<?>> dataClass, Class<?>... classes) {
        DataConverterType dataConverterType = new DataConverterType(dataClass);

        for (Class<?> clazz : classes) {
            DATA_TYPES.put(clazz, field -> dataConverterType);
        }
        return dataConverterType;
    }

    public static void register(Function<Field, DataConverterType> function, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            DATA_TYPES.put(clazz, function);
        }
    }

    public static DataConverterType getType(Field field) {

        Class<?> type = field.getType();
        if (!DATA_TYPES.containsKey(type)) {
            throw new UnsupportedOperationException("Type " + type.getSimpleName() + " doesn't supported for converters!");
        }
        return DATA_TYPES.get(type).apply(field);

    }

    public static DataConverterType getType(Class<?> clazz) {
        return DATA_TYPES.get(clazz).apply(null);
    }

    private static class ListDataConverterType extends DataConverterType {

        private final DataConverterType insideType;

        public ListDataConverterType(Field field) {
            super(ListDataConverter.class);

            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            this.insideType = DataConverterType.getType((Class<?>) parameterizedType.getActualTypeArguments()[0]);
        }

        @Override
        public DataConverter<?> newData() {
            ListDataConverter listData = new ListDataConverter();
            listData.setType(this.insideType);
            return listData;
        }

    }


}
