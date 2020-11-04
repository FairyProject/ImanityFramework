package org.imanity.framework.cache.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class CacheKeyMethod extends CacheKeyAbstract {

    private final Method method;
    private final Object[] arguments;

    public CacheKeyMethod(final JoinPoint point) {
        super(point);
        this.method = ((MethodSignature) point.getSignature()).getMethod();
        this.arguments = point.getArgs();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(this.method);
    }

    @Override
    public final boolean equals(Object object) {
        boolean equals;
        if (this == object) {
            equals = true;
        } else if (object instanceof CacheKeyMethod) {
            CacheKeyMethod key = (CacheKeyMethod) object;
            equals = key.method.equals(this.method)
                    && Arrays.deepEquals(key.arguments, this.arguments);
        } else {
            equals = false;
        }
        return equals && super.equals(object);
    }

}
