package org.imanity.framework.util;

import java.lang.reflect.Constructor;

public class ReflectionUtil {

    public static <T> Constructor<T> getConstructor(Class<T> parentClass, Class<?>... parameterTypes) {
        try {
            return parentClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static <T> Constructor<T> lookupConstructors(Class<T> parentClass, Class<?>[]... parameters) {
        for (Class<?>[] parameter : parameters) {
            Constructor<T> constructor = ReflectionUtil.getConstructor(parentClass, parameter);

            if (constructor == null) {
                continue;
            }

            return constructor;
        }

        return null;
    }

}
