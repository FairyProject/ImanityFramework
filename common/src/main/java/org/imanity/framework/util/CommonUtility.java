package org.imanity.framework.util;

import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;

public class CommonUtility {

    public static <T> Constructor<T> getConstructor(Class<T> parentClass, Class<?>... parameterTypes) {
        try {
            return parentClass.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public static <T> String joinToString(final T[] array) {
        return array == null ? "null" : joinToString(Arrays.asList(array));
    }

    public static <T> String joinToString(final T[] array, final String delimiter) {
        return array == null ? "null" : joinToString(Arrays.asList(array), delimiter);
    }

    public static <T> String joinToString(final Iterable<T> array) {
        return array == null ? "null" : joinToString(array, ", ");
    }

    public static <T> String joinToString(final Iterable<T> array, final String delimiter) {
        return join(array, delimiter, object -> object == null ? "" : object.toString());
    }

    public static <T> String join(final Iterable<T> array, final String delimiter, final Stringer<T> stringer) {
        final Iterator<T> it = array.iterator();
        String message = "";

        while (it.hasNext()) {
            final T next = it.next();

            if (next != null)
                message += stringer.toString(next) + (it.hasNext() ? delimiter : "");
        }

        return message;
    }

    public interface Stringer<T> {

        /**
         * Convert the given object into a string
         *
         * @param object
         * @return
         */
        String toString(T object);
    }


}
