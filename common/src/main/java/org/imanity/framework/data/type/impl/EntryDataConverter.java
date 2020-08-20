package org.imanity.framework.data.type.impl;

import org.imanity.framework.util.builder.SQLColumnType;
import org.bson.Document;
import org.imanity.framework.data.type.DataConverter;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.util.entry.Entry;

import java.lang.reflect.Field;

public class EntryDataConverter extends AbstractDataConverter<Entry> {

    private Entry<DataConverter, DataConverter> entry;

    @Override
    public Object get() {
        Document document = new Document();
        document.put("key", entry.getKey().get());
        document.put("value", entry.getValue().get());

        document.put("keyClass", entry.getKey().getType().getName());
        document.put("valueClass", entry.getValue().getType().getName());
        return document;
    }

    @Override
    public void set(Object object) {
        if (object instanceof Entry) {

            Entry other = (Entry) object;

            Object key = other.getKey();
            DataConverterType keyType = DataConverterType.getType(key.getClass());

            if (keyType == null) {
                throw new IllegalStateException("The key type " + key.getClass().getSimpleName() + " does not exists!");
            }

            Object value = other.getValue();
            DataConverterType valueType = DataConverterType.getType(value.getClass());

            if (valueType == null) {
                throw new IllegalStateException("The value type " + key.getClass().getSimpleName() + " does not exists!");
            }

            DataConverter keyData = keyType.newData(), valueData = valueType.newData();
            keyData.set(key);
            valueData.set(key);

            entry = new Entry<>(keyData, valueData);

        } else if (object instanceof Document) {

            Document document = (Document) object;

            try {
                Class<?> keyClass = Class.forName(document.getString("keyClass"));
                Class<?> valueClass = Class.forName(document.getString("valueClass"));

                DataConverterType keyType = DataConverterType.getType(keyClass);
                if (keyType == null) {
                    throw new IllegalStateException("The key type " + keyClass.getSimpleName() + " does not exists!");
                }

                DataConverterType valueType = DataConverterType.getType(valueClass);

                if (valueType == null) {
                    throw new IllegalStateException("The value type " + valueClass.getSimpleName() + " does not exists!");
                }

                DataConverter keyData = keyType.newData(), valueData = valueType.newData();
                keyData.set(document.get("key", keyClass));
                valueData.set(document.get("value", valueClass));

                entry = new Entry<>(keyData, valueData);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Something wrong while reading entry data", ex);
            }

        }

        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to Entry");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.MEDIUMTEXT;
    }

    @Override
    public Class<?> getLoadType() {
        return Document.class;
    }

    @Override
    public Object toFieldObject(Field field) {
        return new Entry<>(entry.getKey().get(), entry.getValue().get());
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + ((Document) this.get()).toJson() + (sql ? "\"" : "");
    }
}
