package org.imanity.framework.player.data.type.impl;

import lombok.Setter;
import org.imanity.framework.util.builder.SQLColumnType;
import org.imanity.framework.player.data.type.Data;
import org.imanity.framework.player.data.type.DataType;
import org.imanity.framework.util.CommonUtility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListData extends AbstractData<List<Data<?>>> {

    private List<Data<?>> list;
    @Setter
    private DataType type;

    @Override
    public Object get() {
        return this.list.stream()
                .map(Data::get)
                .collect(Collectors.toList());
    }

    @Override
    public Class<?> getType() {
        return List.class;
    }

    @Override
    public void set(Object object) {
        if (object instanceof String) {

            List<Data<?>> list = new ArrayList<>();
            String[] strings = ((String) object).split("(?=(([^\"]+\"){2})*[^\"]*$)[^a-zA-Z\\d]+");

            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];

                if (string.startsWith("\"") && string.endsWith("\"")) {
                    string = string.substring(1, string.length() - 1);
                }

                Data<?> data = type.newData();
                data.set(string);

                list.add(data);
            }

            this.list = list;
            return;
        } else if (object instanceof List) {

            List<Data<?>> list = new ArrayList<>();
            for (Object obj : (List) object) {

                Data<?> data = type.newData();
                data.set(obj);

                list.add(data);

            }

            this.list = list;
            return;
        }
        throw new UnsupportedOperationException(object.getClass().getSimpleName() + " cannot be case to List");
    }

    @Override
    public Object toFieldObject(Field field) {
        Class<?> type = field.getType();

        if (List.class.isAssignableFrom(type)) {

            Class<? extends List> listType = type.asSubclass(List.class);

            if (listType == List.class) {
                listType = ArrayList.class;
            }

            try {
                List list = listType.newInstance();

                this.list.stream()
                        .map(Data::get)
                        .forEach(list::add);

                return list;
            } catch (Exception ex) {
                throw new IllegalStateException("Something wrong while converting list data!", ex);
            }
        }

        throw new IllegalStateException("The field " + field.getName() + " isn't a list!");
    }

    @Override
    public SQLColumnType columnType() {
        return SQLColumnType.LONGTEXT;
    }

    @Override
    public String toStringData(boolean sql) {
        return (sql ? "\"" : "") + CommonUtility.join(list, "^", data -> data.toStringData(true)) + (sql ? "\"" : "");
    }
}
