package org.imanity.framework.cache.impl;

import org.aspectj.lang.JoinPoint;

import java.util.Objects;
import java.util.StringJoiner;

public class CacheKeyString extends CacheKeyAbstract {

    private final String key;

    public CacheKeyString(JoinPoint point, String key) {
        super(point);
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKeyString that = (CacheKeyString) o;

        return Objects.equals(key, that.key) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CacheKeyString.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .toString();
    }

}
