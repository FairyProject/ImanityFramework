package org.imanity.framework.bukkit.reflection.wrapper;

import com.sk89q.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.util.EquivalentConverter;

import java.lang.reflect.Field;

@Getter
@Setter
@AllArgsConstructor
public class WatchableObjectWrapper extends WrapperAbstract {
    private int index;
    private int typeId;
    private Object value;
    private boolean dirty;

    public static Class<?> WATCHABLE_CLASS = new NMSClassResolver().resolveSilent("DataWatcher$WatchableObject");

    private static FieldResolver WATCHABLE_OBJECT_RESOLVER = new FieldResolver(WATCHABLE_CLASS);

    private static final Field TYPE_ID_FIELD = WATCHABLE_OBJECT_RESOLVER.resolveIndexSilent(0);
    private static final Field INDEX_FIELD = WATCHABLE_OBJECT_RESOLVER.resolveIndexSilent(1);
    private static final Field VALUE_FIELD = WATCHABLE_OBJECT_RESOLVER.resolveIndexSilent(2);
    private static final Field DIRTY_FIELD = WATCHABLE_OBJECT_RESOLVER.resolveIndexSilent(3);

    @SneakyThrows
    public WatchableObjectWrapper(Object handle) {
        this.typeId = TYPE_ID_FIELD.getInt(handle);
        this.index = INDEX_FIELD.getInt(handle);
        this.value = VALUE_FIELD.get(handle);
        this.dirty = DIRTY_FIELD.getBoolean(handle);
    }

    @Override
    public boolean exists() {
        return true;
    }

    private static EquivalentConverter<WatchableObjectWrapper> CONVERTER;

    public static EquivalentConverter<WatchableObjectWrapper> getConverter() {
        if (CONVERTER != null) return CONVERTER;

        return CONVERTER = new EquivalentConverter<WatchableObjectWrapper>() {
            @SneakyThrows
            @Override
            public Object getGeneric(WatchableObjectWrapper specific) {
                Object handle = WATCHABLE_CLASS.getConstructor(int.class, int.class, Object.class).newInstance(specific.typeId, specific.index, specific.value);
                DIRTY_FIELD.set(handle, specific.dirty);

                return handle;
            }

            @Override
            public WatchableObjectWrapper getSpecific(Object generic) {
                return new WatchableObjectWrapper(generic);
            }

            @Override
            public Class<WatchableObjectWrapper> getSpecificType() {
                return WatchableObjectWrapper.class;
            }
        };
    }
}
