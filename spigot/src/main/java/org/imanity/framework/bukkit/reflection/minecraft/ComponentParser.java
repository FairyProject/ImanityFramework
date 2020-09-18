package org.imanity.framework.bukkit.reflection.minecraft;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ComponentParser {
    private static Constructor readerConstructor;
    private static Method setLenient;
    private static Method getAdapter;
    private static Method read;

    private ComponentParser() {
    }

    public static Object deserialize(Object gson, Class<?> component, StringReader str) {
        try {
            JsonReader reader = new JsonReader(str);
            reader.setLenient(true);
            return ((Gson)gson).getAdapter(component).read(reader);
        } catch (IOException var4) {
            throw new RuntimeException("Failed to read JSON", var4);
        } catch (LinkageError var5) {
            return deserializeLegacy(gson, component, str);
        }
    }

    private static Object deserializeLegacy(Object gson, Class<?> component, StringReader str) {
        try {
            Object adapter;
            if (readerConstructor == null) {
                Class<?> readerClass = Class.forName("org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader");
                readerConstructor = readerClass.getDeclaredConstructor(Reader.class);
                readerConstructor.setAccessible(true);
                setLenient = readerClass.getDeclaredMethod("setLenient", Boolean.TYPE);
                setLenient.setAccessible(true);
                getAdapter = gson.getClass().getDeclaredMethod("getAdapter", Class.class);
                getAdapter.setAccessible(true);
                adapter = getAdapter.invoke(gson, component);
                read = adapter.getClass().getDeclaredMethod("read", readerClass);
                read.setAccessible(true);
            }

            Object reader = readerConstructor.newInstance(str);
            setLenient.invoke(reader, true);
            adapter = getAdapter.invoke(gson, component);
            return read.invoke(adapter, reader);
        } catch (ReflectiveOperationException var5) {
            throw new RuntimeException("Failed to read JSON", var5);
        }
    }
}
