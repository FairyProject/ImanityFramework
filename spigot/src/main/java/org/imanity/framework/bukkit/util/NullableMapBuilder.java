package org.imanity.framework.bukkit.util;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;

public class NullableMapBuilder<K, V> extends ImmutableMap.Builder<K, V> {

    public NullableMapBuilder() {
        super();
    }

    @Override
    public ImmutableMap.Builder<K, V> put(@Nullable K key, V value) {
        if (key == null) {
            return this;
        }
        return super.put(key, value);
    }
}
