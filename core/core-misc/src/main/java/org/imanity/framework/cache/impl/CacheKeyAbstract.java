/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
