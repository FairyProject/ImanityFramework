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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.exception.ServiceAlreadyExistsException;
import org.imanity.framework.factory.ClassFactory;
import org.imanity.framework.factory.WiredFieldFactory;
import org.imanity.framework.util.AccessUtil;
import org.imanity.framework.util.entry.Entry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceHandler {

    private static final Logger LOGGER = LogManager.getLogger(ServiceHandler.class);

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

    public void registerServices() {

        try {

            Map<String, ServiceData> services = new HashMap<>();
            for (Class<?> type : ClassFactory.getClasses(Service.class)) {

                Service service = type.getAnnotation(Service.class);
                Preconditions.checkNotNull(service, "The type " + type.getName() + " doesn't have @Service annotation!");

                Object instance;
                try {
                    instance = ConstructorUtils.invokeConstructor(type);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new IllegalArgumentException("Something wrong while creating instance of " + type.getSimpleName() + " (no args constructor not exists?!)", e);
                }

                String name = service.name();

                if (!services.containsKey(name)) {
                    services.put(name, new ServiceData(instance.getClass(), instance, name, service.dependencies(), true));
                } else {
                    new ServiceAlreadyExistsException(name).printStackTrace();
                }
            }

            this.sort(services);
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }

        for (Entry<String, Object> entry : FrameworkMisc.PLATFORM.getPluginInstances()) {
            this.registerService(entry.getValue(), entry.getKey(), new String[0], false);
        }

    }

    private void initialAutowired() {
        WiredFieldFactory.loadFields();

        this.getServiceInstances().forEach(this::registerAutowired);

        try {
            Collection<Field> fields = WiredFieldFactory.getStaticFields(Autowired.class);
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                AccessUtil.setAccessible(field);

                Object service = this.getServiceInstance(field.getType());
                field.set(null, service);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }
    }

    @SneakyThrows
    public void registerAutowired(Object instance) {
        Collection<Field> fields = WiredFieldFactory.getFields(Autowired.class, instance.getClass());
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object service = this.getServiceInstance(field.getType());

            if (service != null) {
                AccessUtil.setAccessible(field);
                field.set(instance, service);
            } else {
                throw new IllegalArgumentException("Couldn't find bean " + field.getType().getName() + " !");
            }
        }
    }

    @SneakyThrows
    public void init() {
        this.call(PreInitialize.class);

        this.initialAutowired();
        ComponentRegistry.loadComponents(this);

        this.call(PostInitialize.class);

        FrameworkMisc.EVENT_HANDLER.onPostServicesInitial();
    }

    @SneakyThrows
    public void stop() {
        this.call(PreDestroy.class);

        // I don't have any idea what should i do on shutdown

        this.call(PostDestroy.class);
    }

    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        for (ServiceData serviceData : this.getServices()) {
            serviceData.call(annotation);
        }
    }

    private void sort(Map<String, ServiceData> services) {

        List<ServiceData> unloaded = new ArrayList<>(services.values());
        Set<String> existing = unloaded.stream()
                .map(ServiceData::getName)
                .collect(Collectors.toSet());

        // Remove Services without valid dependency
        Iterator<ServiceData> removeIterator = unloaded.iterator();
        while (removeIterator.hasNext()) {
            ServiceData data = removeIterator.next();

            for (String dependency : data.getDependencies()) {
                if (!existing.contains(dependency)) {
                    LOGGER.error("Couldn't find the dependency " + dependency + " for " + data.getName() + "!");
                    removeIterator.remove();
                    break;
                }
            }
        }

        existing.clear();

        // Continually loop until all dependency found and loaded
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

    public void registerService(Object serviceInstance, String name, String[] dependencies, boolean callAnnotations) {
        this.services.put(serviceInstance.getClass(), new ServiceData(serviceInstance.getClass(), serviceInstance, name, dependencies, callAnnotations));
    }

}
