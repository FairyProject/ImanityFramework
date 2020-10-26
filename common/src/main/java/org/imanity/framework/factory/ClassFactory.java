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

package org.imanity.framework.factory;

import lombok.experimental.UtilityClass;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.annotation.ClasspathScan;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.util.entry.Entry;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public final class ClassFactory {

    private static final Map<Class<? extends Annotation>, Collection<Class<?>>> CLASSES = new ConcurrentHashMap<>(128);

    public static String[] CLASS_PATHS;

    public static void loadClasses() {

        List<String> classPaths = new ArrayList<>();
        classPaths.add("org.imanity.framework");

        for (Entry<String, Object> entry : ImanityCommon.BRIDGE.getPluginInstances()) {
            Object instance = entry.getValue();
            Class<?> type = instance.getClass();

            ClasspathScan annotation = type.getAnnotation(ClasspathScan.class);

            if (annotation != null) {
                classPaths.add(annotation.value());
            }
        }

        ClassFactory.CLASS_PATHS = classPaths.toArray(new String[0]);

        Reflections reflections = new Reflections(ClassFactory.CLASS_PATHS, new TypeAnnotationsScanner());

        ClassFactory.scan(Component.class, reflections);
        ClassFactory.scan(Service.class, reflections);
    }

    public static Collection<Class<?>> getClasses(Class<? extends Annotation> annotation) {
        return CLASSES.getOrDefault(annotation, Collections.emptyList());
    }

    public static void scan(Class<? extends Annotation> annotation) {
        ClassFactory.scan(annotation, new Reflections(ClassFactory.CLASS_PATHS, new TypeAnnotationsScanner()));
    }

    private static void scan(Class<? extends Annotation> annotation, Reflections reflections) {
        CLASSES.put(annotation, reflections.getTypesAnnotatedWith(annotation));
    }

}
