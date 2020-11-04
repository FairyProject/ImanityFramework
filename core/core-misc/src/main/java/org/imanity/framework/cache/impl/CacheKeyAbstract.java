package org.imanity.framework.cache.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public abstract class CacheKeyAbstract {

    private final Object target;

    public CacheKeyAbstract(JoinPoint point) {
        this.target = CacheKeyAbstract.findTarget(point);
    }

    public boolean sameTarget(final JoinPoint point, String key) {
        return CacheKeyAbstract.findTarget(point).equals(this.target);
    }

    public static Object findTarget(final JoinPoint point) {
        Object target;
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            target = method.getDeclaringClass();
        } else {
            target = point.getTarget();
        }
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheKeyAbstract that = (CacheKeyAbstract) o;

        return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return target != null ? target.hashCode() : 0;
    }
}
