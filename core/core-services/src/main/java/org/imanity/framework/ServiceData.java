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
    private String[] dependencies;

    private Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods;

    public ServiceData(Object instance, Service service) {
        this(instance.getClass(), instance, service.name(), service.dependencies());
    }

    public ServiceData(Class<?> type, Object instance, String name, String[] dependencies) {
        this.type = type;
        this.instance = instance;
        this.name = name;
        this.dependencies = dependencies;

        this.loadAnnotatedMethods();
    }

    @SneakyThrows
    public void loadAnnotatedMethods() {
        this.annotatedMethods = new HashMap<>();

        Class<?> type = this.type;
        while (type != null && type != Object.class) {
            for (Method method : type.getDeclaredMethods()) {
                this.loadMethod(method);
            }
            type = type.getSuperclass();
        }
    }

    public void loadMethod(Method method) throws ReflectiveOperationException {
        for (Class<? extends Annotation> annotation : ServiceData.ANNOTATIONS) {
            if (method.getAnnotation(annotation) != null) {

                if (method.getParameterCount() != 0) {
                    throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but having parameters! parameters are not allowed when using " + annotation.getSimpleName() + "!");
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

    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        if (this.annotatedMethods.containsKey(annotation)) {
            for (Method method : this.annotatedMethods.get(annotation)) {
                method.invoke(instance);
            }
        }
    }

    public boolean hasDependencies() {
        return this.dependencies.length > 0;
    }

}
