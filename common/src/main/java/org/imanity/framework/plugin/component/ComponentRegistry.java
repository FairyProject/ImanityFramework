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
                    try {
                        Class<?> componentClass = Class.forName(className);

                        ComponentHolder componentHolder = getComponentHolder(componentClass);
                        Object instance = componentHolder.newInstance(componentClass);

                        if (instance != null) {
                            serviceHandler.registerAutowired(instance);
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
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
