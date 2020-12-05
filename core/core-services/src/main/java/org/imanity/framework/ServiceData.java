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
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import lombok.SneakyThrows;

@Getter
@Setter
public class ServiceData {

    private static final Class<? extends Annotation>[] ANNOTATIONS = new Class[] {
            PreInitialize.class, PostInitialize.class,
            PreDestroy.class, PostDestroy.class
    };

    private Class<?> type;

    private Object instance;

    private String name;
    private List<String> dependencies;
    private boolean callAnnotations;

    private ActivationStage stage;

    private Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods;

    public ServiceData(Object instance, Service service) {
        this(instance.getClass(), instance, service.name(), service.dependencies(), true);
    }

    public ServiceData(Class<?> type, Object instance, String name, String[] dependencies, boolean callAnnotations) {
        this.type = type;
        this.instance = instance;
        this.name = name;
        this.dependencies = Lists.newArrayList(dependencies);
        this.callAnnotations = callAnnotations;
        this.stage = ActivationStage.NOT_LOADED;

        if (callAnnotations) {
            this.loadAnnotations();
        }
    }

    @SneakyThrows
    public void loadAnnotations() {
        this.annotatedMethods = new HashMap<>();

        Class<?> type = this.type;
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

                int parameterCount = method.getParameterCount();
                if (parameterCount > 0) {
                    if (parameterCount != 1 && method.getParameterTypes()[0] != ServiceData.class) {
                        throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but doesn't have matches parameters! you can only use either no parameter or one parameter with ServerData type on annotated " + annotation.getSimpleName() + "!");
                    }
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
