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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.Utility;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.annotation.AnnotationDetector;
import org.imanity.framework.util.entry.Entry;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceHandler {

    private final Map<Class<?>, ServiceData> services = new LinkedHashMap<>();

    public Object getServiceInstance(@NonNull Class<?> type) {
        ServiceData data = this.services.getOrDefault(type, null);
        if (data == null) {
            return null;
        }
        return data.getInstance();
    }

    public Collection<ServiceData> getServices() {
        return this.services.values();
    }

    public Collection<Object> getServiceInstances() {
        return this.getServices().stream()
                .map(ServiceData::getInstance)
                .collect(Collectors.toList());
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

            List<ServiceData> services = new ArrayList<>();

            new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    try {
                        Class<?> type = Class.forName(className);

                        Service service = type.getAnnotation(Service.class);
                        Preconditions.checkNotNull(service, "The type " + type.getName() + " doesn't have @Service annotation!");

                        Object instance;
                        try {
                            instance = ConstructorUtils.invokeConstructor(type);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            throw new RuntimeException("Something wrong while creating instance of " + type.getSimpleName() + " (no args constructor not exists?!)", e);
                        }

                        services.add(new ServiceData(instance, service));
                    } catch (IllegalStateException | ClassNotFoundException ex) {
                        ImanityCommon.getLogger().error(ex);
                    }
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {Service.class};
                }
            }).detect(files.toArray(new File[0]));

            this.sort(services);

        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }

        for (Entry<String, Object> entry : ImanityCommon.BRIDGE.getPluginInstances()) {
            ImanityCommon.getLogger().info("Registering plugin " + entry.getValue().getClass().getSimpleName() + " as service.");
            this.registerService(entry.getValue(), entry.getKey(), new String[0]);
        }

    }

    private void initialAutowired() {
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

        this.initialAutowired();
        ComponentRegistry.loadComponents(this);

        this.getImplementedService().forEach(IService::init);
        ImanityCommon.EVENT_HANDLER.onPostServicesInitial();
    }

    public void stop() {
        this.getImplementedService().forEach(IService::stop);
    }

    private void sort(Collection<ServiceData> services) {

        List<ServiceData> unloaded = new ArrayList<>(services);
        Set<String> existing = services.stream()
                .map(ServiceData::getName)
                .collect(Collectors.toSet());

        for (ServiceData data : services) {
            for (String dependency : data.getDependencies()) {
                if (!existing.contains(dependency)) {
                    ImanityCommon.getLogger().error("Couldn't find the dependency " + dependency + " for " + data.getName() + "!");
                    unloaded.remove(data);
                    break;
                }
            }
        }

        existing.clear();

        Map<String, ServiceData> sorted = new LinkedHashMap<>();

        while (!unloaded.isEmpty()) {
            Iterator<ServiceData> iterator = unloaded.iterator();

            while (iterator.hasNext()) {
                ServiceData data = iterator.next();
                boolean missingDependencies = true;

                if (!data.hasDependencies()) {
                    missingDependencies = false;
                } else {
                    List<String> list = Lists.newArrayList(data.getDependencies());
                    list.removeIf(sorted::containsKey);

                    if (list.isEmpty()) {
                        missingDependencies = false;
                    }
                }

                if (!missingDependencies) {
                    sorted.put(data.getName(), data);
                    iterator.remove();
                }
            }
        }

        for (ServiceData data : sorted.values()) {
            this.services.put(data.getType(), data);
        }
    }

    public void registerService(Object serviceInstance, String name, String[] dependencies) {
        this.services.put(serviceInstance.getClass(), new ServiceData(serviceInstance.getClass(), serviceInstance, name, dependencies));
    }

}
