package org.imanity.framework.bukkit.util.reflection.resolver.wrapper;

import lombok.Getter;
import org.imanity.framework.bukkit.util.reflection.minecraft.DataWatcher;

public class DataWatcherWrapper extends WrapperAbstract {

    public static DataWatcherWrapper create(Object entity) {
        try {
            return new DataWatcherWrapper(DataWatcher.newDataWatcher(entity));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Getter
    private final Object dataWatcherObject;

    public DataWatcherWrapper(Object dataWatcherObject) {
        this.dataWatcherObject = dataWatcherObject;
    }

    public void setValue(int index, DataWatcher.V1_9.ValueType type, Object value) {
        try {
            DataWatcher.setValue(this.dataWatcherObject, index, type, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public <T> T getValue(int index, DataWatcher.V1_9.ValueType type, Class<T> classType) {
        try {
            return (T) DataWatcher.getValue(this.dataWatcherObject, index, type);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Object getValue(int index, DataWatcher.V1_9.ValueType type) {
        try {
            return DataWatcher.getValue(this.dataWatcherObject, index, type);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public boolean exists() {
        return dataWatcherObject != null;
    }
}
