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

import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.details.constructor.BeanConstructorDetails;
import org.imanity.framework.details.constructor.GenericBeanConstructorDetails;
import org.imanity.framework.factory.ClassFactory;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

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

    private static void registerComponentHolders() {
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] {Thread.class};
            }

            @Override
            public Object newInstance(Class<?> type) {
                Thread thread = (Thread) super.newInstance(type);

                FrameworkMisc.TASK_SCHEDULER.runSync(thread::start); // Don't start Immediately
                return thread;
            }
        });
    }

    public static void loadComponents(BeanContext beanContext) {
        try {
            ComponentRegistry.registerComponentHolders();

            for (Class<?> type : ClassFactory.getClasses(Component.class)) {
                ComponentHolder componentHolder = ComponentRegistry.getComponentHolder(type);
                Object instance = componentHolder.newInstance(type);

                if (instance != null) {
                    beanContext.registerComponent(instance, type);
                }
            }
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
