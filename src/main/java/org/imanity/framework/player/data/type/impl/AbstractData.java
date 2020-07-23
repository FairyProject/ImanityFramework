package org.imanity.framework.player.data.type.impl;

import lombok.NoArgsConstructor;
import org.imanity.framework.player.data.type.Data;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

@NoArgsConstructor
public abstract class AbstractData<T> implements Data<T> {

    private String name;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<T> getType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public String toStringData(boolean sql) {
        return String.valueOf(this.get());
    }

    @Override
    public Object toFieldObject(Field field) {
        return this.get();
    }
}
