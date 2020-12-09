/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.SneakyThrows;

import javax.annotation.Nullable;

@Getter
@Setter
public class ServiceData {

    private static final Class<? extends Annotation>[] ANNOTATIONS = new Class[] {
            PreInitialize.class, PostInitialize.class,
            PreDestroy.class, PostDestroy.class,
            ShouldInitialize.class
    };

    private Class<?> type;

    private Object instance;

    private String name;
    private Set<String> dependencies;
    private boolean callAnnotations;

    private ActivationStage stage;

    private Map<String, String> tags;
    private Map<Class<? extends Annotation>, String> disallowAnnotations;
    private Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods;

    public ServiceData(Object instance) {
        this(instance.getClass(), instance, "dummy", new String[0], true);
    }

    public ServiceData(Object instance, Service service) {
        this(instance.getClass(), instance, service.name(), service.dependencies(), true);
    }

    public ServiceData(Class<?> type, Object instance, String name, String[] dependencies, boolean callAnnotations) {
        this.type = type;
        this.instance = instance;
        this.name = name;
        this.dependencies = Sets.newHashSet(dependencies);
        this.callAnnotations = callAnnotations;
        this.stage = ActivationStage.NOT_LOADED;
        this.tags = new ConcurrentHashMap<>(0);

        if (callAnnotations) {
            this.loadAnnotations();
        }
    }

    @SneakyThrows
    public void loadAnnotations() {
        this.annotatedMethods = new HashMap<>();
        this.disallowAnnotations = new HashMap<>();

        Class<?> type = this.type;
        while (type != null && type != Object.class) {
            DisallowAnnotation disallowAnnotation = type.getAnnotation(DisallowAnnotation.class);
            if (disallowAnnotation != null) {
                for (Class<? extends Annotation> annotation : disallowAnnotation.value()) {
                    this.disallowAnnotations.put(annotation, type.getName());
                }
            }

            type = type.getSuperclass();
        }

        type = this.type;
        while (type != null && type != Object.class) {
            for (Method method : type.getDeclaredMethods()) {
                this.loadMethod(method);
            }

            ServiceDependency dependencyAnnotation = type.getAnnotation(ServiceDependency.class);
            if (dependencyAnnotation != null) {
                dependencies.addAll(Arrays.asList(dependencyAnnotation.dependencies()));
            }

            type = type.getSuperclass();
        }
    }

    public void loadMethod(Method method) {
        for (Class<? extends Annotation> annotation : ServiceData.ANNOTATIONS) {
            if (method.getAnnotation(annotation) != null) {
                if (this.disallowAnnotations.containsKey(annotation)) {
                    String className = this.disallowAnnotations.get(annotation);
                    throw new IllegalArgumentException("The annotation " + annotation.getSimpleName() + " is disallowed by class " + className + ", But it used in method " + method.toString());
                }

                int parameterCount = method.getParameterCount();
                if (parameterCount > 0) {
                    if (parameterCount != 1 && method.getParameterTypes()[0] != ServiceData.class) {
                        throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but doesn't have matches parameters! you can only use either no parameter or one parameter with ServerData type on annotated " + annotation.getSimpleName() + "!");
                    }
                }

                if (annotation == ShouldInitialize.class && method.getReturnType() != boolean.class) {
                    throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but doesn't have matches return type! you can only use boolean as return type on annotated " + annotation.getSimpleName() + "!");
                }
                method.setAccessible(true);

                if (this.annotatedMethods.containsKey(annotation)) {
                    this.annotatedMethods.get(annotation).add(method);
                } else {
                    List<Method> methods = new LinkedList<>();
                    methods.add(method);

                    this.annotatedMethods.put(annotation, methods);
                }
                break;
            }
        }
    }

    private void changeStage(Class<? extends Annotation> annotation) {
        if (annotation == PreInitialize.class) {
            this.stage = ActivationStage.PRE_INIT_CALLED;
        } else if (annotation == PostInitialize.class) {
            this.stage = ActivationStage.POST_INIT_CALLED;
        } else if (annotation == PreDestroy.class) {
            this.stage = ActivationStage.PRE_DESTROY_CALLED;
        } else if (annotation == PostDestroy.class) {
            this.stage = ActivationStage.POST_DESTROY_CALLED;
        }
    }

    public boolean isStage(ActivationStage stage) {
        return this.stage == stage;
    }

    public boolean isActivated() {
        return this.stage == ActivationStage.PRE_INIT_CALLED || this.stage == ActivationStage.POST_INIT_CALLED;
    }

    public boolean isDestroyed() {
        return this.stage == ActivationStage.PRE_DESTROY_CALLED || this.stage == ActivationStage.POST_DESTROY_CALLED;
    }

    @Nullable
    public String getTag(String key) {
        return this.tags.getOrDefault(key, null);
    }

    public boolean hasTag(String key) {
        return this.tags.containsKey(key);
    }

    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    public boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException  {
        if (this.annotatedMethods == null) {
            return true;
        }

        if (this.annotatedMethods.containsKey(ShouldInitialize.class)) {
            for (Method method : this.annotatedMethods.get(ShouldInitialize.class)) {
                boolean result;

                if (method.getParameterCount() == 1) {
                    result = (boolean) method.invoke(instance, this);
                } else {
                    result = (boolean) method.invoke(instance);
                }

                if (!result) {
                    return false;
                }
            }
        }

        return true;
    }

    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        if (!this.callAnnotations) {
            return;
        }

        if (this.annotatedMethods.containsKey(annotation)) {
            for (Method method : this.annotatedMethods.get(annotation)) {
                if (method.getParameterCount() == 1) {
                    method.invoke(instance, this);
                } else {
                    method.invoke(instance);
                }
            }
        }

        this.changeStage(annotation);
    }

    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }

    public static enum ActivationStage {

        NOT_LOADED,
        PRE_INIT_CALLED,
        POST_INIT_CALLED,

        PRE_DESTROY_CALLED,
        POST_DESTROY_CALLED

    }

}
