package org.imanity.framework.plugin.component;

import org.imanity.framework.ImanityCommon;
import org.imanity.framework.plugin.service.ServiceHandler;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.Utility;
import org.imanity.framework.util.annotation.AnnotationDetector;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

public class ComponentRegistry {

    private static final EntryArrayList<Class<?>, ComponentHolder> COMPONENT_HOLDERS = new EntryArrayList<>();

    private static final ComponentHolder EMPTY = new ComponentHolderEmpty();

    public static void registerComponentHolder(ComponentHolder componentHolder) {
        for (Class<?> type : componentHolder.type()) {
            COMPONENT_HOLDERS.add(type, componentHolder);
        }
    }

    public static ComponentHolder getComponentHolder(Class<?> type) {

        for (Entry<Class<?>, ComponentHolder> entry : COMPONENT_HOLDERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }

        return EMPTY;

    }

    public static void loadComponents(ServiceHandler serviceHandler) {
        try {
            List<File> files = ImanityCommon.BRIDGE.getPluginFiles();
            files.add(FileUtils.getSelfJar());

            new AnnotationDetector(new AnnotationDetector.TypeReporter() {
                @Override
                public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
                    Utility.tryCatch(() -> {
                        Class<?> componentClass = Class.forName(className);

                        ComponentHolder componentHolder = getComponentHolder(componentClass);
                        Object instance = componentHolder.newInstance(componentClass);

                        serviceHandler.registerAutowired(instance);
                    });
                }

                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {Component.class};
                }
            }).detect(files.toArray(new File[0]));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private static class ComponentHolderEmpty extends ComponentHolder {

        @Override
        public Class<?>[] type() {
            return new Class[0];
        }
    }

}
