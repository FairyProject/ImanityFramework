package org.imanity.framework.player.data.type;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.bson.Document;
import org.imanity.framework.player.data.type.impl.*;
import org.imanity.framework.util.CustomLocation;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataType {

    private static final Map<Class<?>, Function<Field, DataType>> DATA_TYPES = new HashMap<>();

    public static final DataType JSON;
    public static final DataType STRING;
    public static final DataType INT;
    public static final DataType DOUBLE;
    public static final DataType BOOLEAN;
    public static final DataType FLOAT;
    public static final DataType SHORT;
    public static final DataType DOCUMENT;
    public static final DataType ENTRY;
    public static final DataType CUSTOM_LOCATION;

    static {
        JSON = DataType.register(JsonData.class, JsonObject.class);
        STRING = DataType.register(StringData.class, String.class);
        ENTRY = DataType.register(EntryData.class, Entry.class);
        CUSTOM_LOCATION = DataType.register(CustomLocationData.class, CustomLocation.class);

        INT = SHORT = DataType.register(IntData.class,
                int.class,
                short.class,
                Integer.class,
                Short.class
        );
        FLOAT = DOUBLE = DataType.register(DoubleData.class,
                double.class,
                float.class,
                Double.class,
                Float.class
        );
        BOOLEAN = DataType.register(BooleanData.class,
                boolean.class,
                Boolean.class
        );

        DOCUMENT = DataType.register(DocumentData.class, Document.class);

        DataType.register(ListDataType::new,
                List.class,
                ArrayList.class,
                ObjectArrayList.class,
                EntryArrayList.class
                );
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

    public static DataType register(Class<? extends Data<?>> dataClass, Class<?>... classes) {
        DataType dataType = new DataType(dataClass);

        for (Class<?> clazz : classes) {
            DATA_TYPES.put(clazz, field -> dataType);
        }
        return dataType;
    }

    public static void register(Function<Field, DataType> function, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            DATA_TYPES.put(clazz, function);
        }
    }

    public static DataType getType(Field field) {

        Class<?> type = field.getType();
        return DATA_TYPES.get(type).apply(field);

    }

    public static DataType getType(Class<?> clazz) {
        return DATA_TYPES.get(clazz).apply(null);
    }

    private static class ListDataType extends DataType {

        private final DataType insideType;

        public ListDataType(Field field) {
            super(ListData.class);

            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            this.insideType = DataType.getType((Class<?>) parameterizedType.getActualTypeArguments()[0]);
        }

        @Override
        public Data<?> newData() {
            ListData listData = new ListData();
            listData.setType(this.insideType);
            return listData;
        }

    }


}
