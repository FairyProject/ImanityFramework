package org.imanity.framework.data;

import lombok.Getter;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.annotation.StoreData;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.type.DataConverter;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.data.type.DataFieldConvert;
import org.imanity.framework.util.CommonUtility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class AbstractData {

    @StoreData
    @Getter
    private UUID uuid;

    public AbstractData(UUID uuid) {
        this.uuid = uuid;
    }

    public void save() {
        StoreDatabase database = DataHandler.getDatabase(this.getClass());

        database.save(this);
    }

    public List<DataConverter<?>> toDataList(StoreDatabase database) {

        List<DataConverter<?>> dataList = new ArrayList<>();

        database.getFieldConverters().forEach(type -> {
                    try {
                        Field field = type.getField();
                        DataConverter<?> data = type.getType().newData();
                        data.setName(type.getName());

                        data.set(field.get(this));

                        dataList.add(data);
                    } catch (Exception ex) {
                        throw new RuntimeException("Unexpected error on getting field", ex);
                    }
                });

        return dataList;
    }

    public void loadFromDocument(Document document, StoreDatabase database) {
        database.getFieldConverters().forEach(type -> {
            DataConverter<?> data = type.getType().newData();

            if (document.containsKey(type.getName())) {
                try {
                    data.set(document.get(type.getName(), data.getLoadType()));

                    Field field = type.getField();
                    field.set(this, data.toFieldObject(field));
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Unexpected error while reading json files", ex);
                }
            }
        });
    }

    public static List<DataFieldConvert> getDataTypes(Class<? extends AbstractData> playerDataClass) {
        List<DataFieldConvert> types = new ArrayList<>();

        for (Field field : CommonUtility.getAllFields(playerDataClass)) {

            field.setAccessible(true);
            if (field.getAnnotation(StoreData.class) == null) {
                continue;
            }

            if (Modifier.isFinal(field.getModifiers())) {
                new IllegalStateException("The field " + field.getName() + " is final but it's storing datas!").printStackTrace();
                continue;
            }

            if (Modifier.isStatic(field.getModifiers())) {
                new IllegalStateException("The field " + field.getName() + " is static but it's storing datas!").printStackTrace();
                continue;
            }

            DataConverterType dataConverterType = DataConverterType.getType(field);

            if (dataConverterType == null) {
                ImanityCommon.BRIDGE.getLogger().error("The data type " + field.getType().getSimpleName() + " does not exists!");
                continue;
            }

            types.add(new DataFieldConvert(field.getName(), dataConverterType, field));

        }

        return types;
    }

}
