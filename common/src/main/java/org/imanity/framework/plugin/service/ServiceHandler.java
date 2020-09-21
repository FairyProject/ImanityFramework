package org.imanity.framework.plugin.service;

import lombok.NonNull;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.events.annotation.AutoWiredListener;
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

    public void registerListeners() {
        try {
            List<File> files = ImanityCommon.BRIDGE.getPluginFiles();
            files.add(FileUtils.getSelfJar());

            new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    Object listener = ImanityCommon.EVENT_HANDLER.registerWiredListener(className);

                    if (listener != null)
                        registerAutowired(listener);
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {AutoWiredListener.class};
                }
            }).detect(files.toArray(new File[0]));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void registerAutowiredForField(Field field) throws ReflectiveOperationException {
        AccessUtil.setAccessible(field);

        if (field.getAnnotation(Autowired.class) == null) {
            return;
        }

        if (!Modifier.isStatic(field.getModifiers())) {
            return;
        }

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
        this.registerAutowireds();
        this.registerListeners();

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
