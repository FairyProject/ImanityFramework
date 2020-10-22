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

package org.imanity.framework.plugin.service;

import lombok.NonNull;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.Utility;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.annotation.AnnotationDetector;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceHandler {

    private final Map<Class<?>, Object> services = new HashMap<>();

    public Object getServiceInstance(@NonNull Class<?> type) {
        return this.services.getOrDefault(type, null);
    }

    public Collection<Object> getServiceInstances() {
        return this.services.values();
    }

    public Collection<? extends IService> getImplementedService() {
        return this.getServiceInstances()
                .stream()
                .filter(object -> object instanceof IService)
                .map(object -> (IService) object)
                .collect(Collectors.toList());
    }

    public void registerServices() {

        try {
            List<File> files = ImanityCommon.BRIDGE.getPluginFiles();
            files.add(FileUtils.getSelfJar());

            new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    try {
                        Class<?> type = Class.forName(className);

                        registerService(type);
                    } catch (IllegalStateException | ClassNotFoundException ex) {
                        ImanityCommon.getLogger().error(ex);
                    }
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {Service.class};
                }
            }).detect(files.toArray(new File[0]));


        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }

        for (Object plugin : ImanityCommon.BRIDGE.getPluginInstances()) {
            ImanityCommon.getLogger().info("Registering plugin " + plugin.getClass().getSimpleName() + " as service.");
            this.registerService(plugin);
        }

    }

    private void registerAutowireds() {
        this.getServiceInstances().forEach(this::registerAutowired);

        try {
            List<File> files = ImanityCommon.BRIDGE.getPluginFiles();
            files.add(FileUtils.getSelfJar());

            new AnnotationDetector(new AnnotationDetector.FieldReporter() {
                @Override
                public void reportFieldAnnotation(Class<? extends Annotation> annotation, String className, String fieldName) {

                    Utility.tryCatch(() -> {
                        Class<?> type = Class.forName(className);
                        Field field = type.getDeclaredField(fieldName);

                        registerAutowiredForField(field);
                    });

                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {Autowired.class};
                }
            }).detect(files.toArray(new File[0]));


        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }
    }

    public void registerAutowiredForField(Field field) throws ReflectiveOperationException {
        if (field.getAnnotation(Autowired.class) == null) {
            return;
        }
        if (!Modifier.isStatic(field.getModifiers())) {
            return;
        }

        AccessUtil.setAccessible(field);

        Object service = this.getServiceInstance(field.getType());
        field.set(null, service);
    }

    public void registerAutowired(Object instance) {
        Utility.tryCatch(() -> {
            for (Field field : instance.getClass().getDeclaredFields()) {
                AccessUtil.setAccessible(field);

                if (field.getAnnotation(Autowired.class) == null) {
                    continue;
                }

                Object service = this.getServiceInstance(field.getType());

                field.set(instance, service);
            }
        });
    }

    public void init() {
        this.getImplementedService().forEach(IService::preInit);

        this.registerAutowireds();
        ComponentRegistry.loadComponents(this);

        this.getImplementedService().forEach(IService::init);

        ImanityCommon.EVENT_HANDLER.onPostServicesInitial();
    }

    public void stop() {
        this.getImplementedService().forEach(IService::stop);
    }

    private void registerService(Class<?> type) {

        Object instance;
        try {
            instance = ConstructorUtils.invokeConstructor(type);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Something wrong while creating instance of " + type.getSimpleName() + " (no args constructor not exists?!)", e);
        }

        this.services.put(type, instance);

    }

    public void registerService(Object serviceInstance) {
        this.services.put(serviceInstance.getClass(), serviceInstance);
    }

}
