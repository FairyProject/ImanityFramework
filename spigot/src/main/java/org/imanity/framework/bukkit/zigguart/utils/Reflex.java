package org.imanity.framework.bukkit.zigguart.utils;

import java.lang.reflect.Field;

public class Reflex {

    public static <T> T getValue(Object object, String field, Class<T> clazz) {
        return (T) getValue(object, field);
    }

    public static Object getValue(Object object, String field) {
        try {
            Field f = object.getClass().getDeclaredField(field);
            if(!f.isAccessible()) f.setAccessible(true);
            return f.get(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
