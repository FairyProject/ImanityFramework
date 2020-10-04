package org.imanity.framework.bukkit.reflection.accessor;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.imanity.framework.bukkit.reflection.resolver.ResolverQuery;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClassAccessorCache {

    private static final LoadingCache<Class<?>, ClassAccessorCache> CLASS_ACCESSORS = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(ClassAccessorCache::new);

    public static ClassAccessorCache get(Class<?> parentClass) {
        return CLASS_ACCESSORS.get(parentClass);
    }

    private final Class<?> parentClass;
    private final Map<ResolverQuery, Method> methodCache;
    private final Map<ResolverQuery, Field> fieldCache;

    public ClassAccessorCache(Class<?> parentClass) {
        this.parentClass = parentClass;

        this.methodCache = new ConcurrentHashMap<>();
        this.fieldCache = new ConcurrentHashMap<>();
    }

    public Method resolveMethod(ResolverQuery query) throws ReflectiveOperationException {
        if (methodCache.containsKey(query)) {
            return methodCache.get(query);
        }

        int currentIndex = 0;
        Method result = null;
        for (Method method : this.parentClass.getDeclaredMethods()) {
            if ((query.getReturnType() == null || Utility.wrapPrimitive(query.getReturnType()).equals(Utility.wrapPrimitive(method.getReturnType())))
                    && (query.getName() == null || method.getName().equals(query.getName()))
                    && (query.getModifierOptions() == null || query.getModifierOptions().matches(method.getModifiers()))) {
                if (query.getTypes() != null && query.getTypes().length > 0) {
                    if (!Utility.isParametersEquals(method.getParameterTypes(), query.getTypes())) {
                        continue;
                    }
                }

                if (query.getIndex() == -2) {
                    result = method;
                    continue;
                }

                if (query.getIndex() < 0 || query.getIndex() == currentIndex++) {
                    return this.cache(query, method);
                }
            }
        }
        if (result != null) {
            return this.cache(query, result);
        }

        throw new NoSuchMethodException();
    }

    public Field resolveField(ResolverQuery query) throws ReflectiveOperationException {
        if (fieldCache.containsKey(query)) {
            return fieldCache.get(query);
        }

        int currentIndex = 0;
        Field result = null;
        for (Field field : this.parentClass.getDeclaredFields()) {
            if ((query.getName() == null || field.getName().equals(query.getName()))
                    && (query.getReturnType() == null || Utility.wrapPrimitive(query.getReturnType()).equals(Utility.wrapPrimitive(field.getType())))
                    && (query.getModifierOptions() == null || query.getModifierOptions().matches(field.getModifiers()))) {
                if (query.getTypes() != null && query.getTypes().length > 0) {
                    Type[] genericTypes = Utility.getGenericTypes(field);
                    if (genericTypes == null) {
                        continue;
                    }

                    if (!Utility.isParametersEquals(genericTypes, query.getTypes())) {
                        continue;
                    }
                }

                if (query.getIndex() == -2) {
                    result = field;
                    continue;
                }

                if (query.getIndex() < 0 || query.getIndex() == currentIndex++) {
                    return this.cache(query, field);
                }
            }
        }

        if (result != null) {
            return this.cache(query, result);
        }
        throw new NoSuchFieldException();
    }

    private Method cache(ResolverQuery query, Method method) throws ReflectiveOperationException {
        AccessUtil.setAccessible(method);
        this.methodCache.put(query, method);
        return method;
    }

    private Field cache(ResolverQuery query, Field field) throws ReflectiveOperationException {
        AccessUtil.setAccessible(field);
        this.fieldCache.put(query, field);
        return field;
    }
}
