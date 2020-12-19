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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.details.BeanDetails;
import org.imanity.framework.details.GenericBeanDetails;
import org.imanity.framework.details.ServiceBeanDetails;
import org.imanity.framework.details.SimpleBeanDetails;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BeanContext {

    public static BeanContext INSTANCE;
    private static final Logger LOGGER = LogManager.getLogger(BeanContext.class);

    private final Map<Class<?>, BeanDetails> beans = new LinkedHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public BeanDetails getBeanDetails(Class<?> type) {
        this.lock.readLock().lock();
        BeanDetails details = this.beans.getOrDefault(type, null);
        this.lock.readLock().unlock();
        return details;
    }

    public Object getBean(@NonNull Class<?> type) {
        BeanDetails details = this.getBeanDetails(type);
        if (details == null) {
            return null;
        }
        return details.getInstance();
    }

    public BeanDetails[] getBeans() {
        this.lock.readLock().lock();
        BeanDetails[] details = this.beans.values().toArray(new BeanDetails[0]);
        this.lock.readLock().unlock();
        return details;
    }

    public Collection<Object> getServiceInstances() {
        List<Object> instances = new ArrayList<>();
        for (BeanDetails details : this.getBeans()) {
            instances.add(details.getInstance());
        }
        return instances;
    }

    public void registerServices() {

        try {

            Map<String, ServiceBeanDetails> services = new HashMap<>();
            for (Class<?> type : ClassFactory.getClasses(Service.class)) {

                Service service = type.getAnnotation(Service.class);
                Preconditions.checkNotNull(service, "The type " + type.getName() + " doesn't have @Service annotation!");

                String name = service.name();

                if (!services.containsKey(name)) {
                    services.put(name, new ServiceBeanDetails(type, name, service.dependencies()));
                } else {
                    new ServiceAlreadyExistsException(name).printStackTrace();
                }
            }

            this.sort(services);
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }

        for (Entry<String, Object> entry : FrameworkMisc.PLATFORM.getPluginInstances()) {
            this.registerBean(new SimpleBeanDetails(entry.getValue(), entry.getKey(), entry.getValue().getClass()));
        }

    }

    private void initialBeans() {
        WiredFieldFactory.loadFields();

        this.getServiceInstances().forEach(this::injectBeans);

        try {
            Collection<Field> fields = WiredFieldFactory.getStaticFields(Autowired.class);
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                AccessUtil.setAccessible(field);

                Object service = this.getBean(field.getType());
                field.set(null, service);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong will detecting @Service annotation", throwable);
        }
    }

    @SneakyThrows
    public void injectBeans(Object instance) {
        Collection<Field> fields = WiredFieldFactory.getFields(Autowired.class, instance.getClass());

        for (Field field : fields) {

            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }

            Object service = this.getBean(field.getType());

            if (service != null) {
                AccessUtil.setAccessible(field);
                field.set(instance, service);
            } else {
                throw new IllegalArgumentException("Couldn't find bean " + field.getType().getName() + " !");
            }
        }
    }

    @SneakyThrows
    public void registerComponent(Object instance, Class<?> type) {
        Component annotation = type.getAnnotation(Component.class);
        if (annotation == null) {
            throw new IllegalArgumentException("The type " + type.getName() + " doesn't have Component annotation!");
        }

        String name = annotation.value();
        if (name.length() == 0) {
            name = instance.getClass().getName();
        }
        BeanDetails details = this.registerBean(instance, name);

        details.call(PreInitialize.class);
        this.injectBeans(instance);
    }

    @SneakyThrows
    public void init() {
        INSTANCE = this;

        this.lock.writeLock().lock();
        Iterator<Map.Entry<Class<?>, BeanDetails>> iterator = this.beans.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, BeanDetails> entry = iterator.next();
            if (!entry.getValue().shouldInitialize()) {
                iterator.remove();
            }
        }
        this.lock.writeLock().unlock();

        this.registerBean(new SimpleBeanDetails(this, "beanContext", this.getClass()));
        this.call(PreInitialize.class);

        this.initialBeans();
        ComponentRegistry.loadComponents(this);

        this.call(PostInitialize.class);

        FrameworkMisc.EVENT_HANDLER.onPostServicesInitial();
    }

    @SneakyThrows
    public void stop() {
        this.callReverse(PreDestroy.class);

        // I don't have any idea what should i do on shutdown

        this.callReverse(PostDestroy.class);
    }

    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        for (BeanDetails beanDetails : this.getBeans()) {
            beanDetails.call(annotation);
        }
    }

    // call reversely so dependencies would shutdown later
    public void callReverse(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        List<BeanDetails> details = Lists.newArrayList(this.getBeans());
        Collections.reverse(details);
        for (BeanDetails beanDetails : details) {
            beanDetails.call(annotation);
        }
    }

    private void sort(Map<String, ServiceBeanDetails> services) {

        Map<String, ServiceBeanDetails> unloaded = new HashMap<>(services);

        this.lock.writeLock().lock();
        for (ServiceBeanDetails beanDetails : unloaded.values()) {
            this.beans.put(beanDetails.getType(), beanDetails);
        }
        this.lock.writeLock().unlock();

        for (ServiceBeanDetails beanDetails : unloaded.values()) {
            beanDetails.setupConstruction(this);
        }

        // Remove Services without valid dependency
        Iterator<Map.Entry<String, ServiceBeanDetails>> removeIterator = unloaded.entrySet().iterator();
        while (removeIterator.hasNext()) {
            Map.Entry<String, ServiceBeanDetails> entry = removeIterator.next();
            ServiceBeanDetails beanDetails = entry.getValue();

            if (!beanDetails.hasDependencies()) {
                continue;
            }

            for (String dependency : beanDetails.getDependencies()) {
                if (!services.containsKey(dependency)) {
                    LOGGER.error("Couldn't find the dependency " + dependency + " for " + beanDetails.getName() + "!");
                    removeIterator.remove();
                    break;
                } else {
                    // Prevent dependency each other
                    ServiceBeanDetails dependencyDetails = services.get(dependency);

                    if (dependencyDetails.hasDependencies() && dependencyDetails.getDependencies().contains(beanDetails.getName())) {
                        LOGGER.error("Target " + beanDetails.getName() + " and " + dependency + " depend to each other!");
                        removeIterator.remove();

                        unloaded.remove(dependency);
                        break;
                    }
                }
            }
        }

        // Continually loop until all dependency found and loaded
        Map<String, ServiceBeanDetails> sorted = new LinkedHashMap<>();

        while (!unloaded.isEmpty()) {
            Iterator<ServiceBeanDetails> iterator = unloaded.values().iterator();

            while (iterator.hasNext()) {
                ServiceBeanDetails beanDetails = iterator.next();
                boolean missingDependencies = true;

                if (!beanDetails.hasDependencies()) {
                    missingDependencies = false;
                } else {
                    List<String> list = Lists.newArrayList(beanDetails.getDependencies());
                    list.removeIf(sorted::containsKey);

                    if (list.isEmpty()) {
                        missingDependencies = false;
                    }
                }

                if (!missingDependencies) {
                    beanDetails.build(this);

                    sorted.put(beanDetails.getName(), beanDetails);
                }
            }
        }

        this.lock.writeLock().lock();
        this.beans.clear();

        for (BeanDetails data : sorted.values()) {
            this.beans.put(data.getType(), data);
        }
        this.lock.writeLock().unlock();
    }

    public BeanDetails registerBean(Object serviceInstance, String name) {
        BeanDetails details = new GenericBeanDetails(serviceInstance.getClass(), serviceInstance, name);
        return this.registerBean(details);
    }

    public BeanDetails registerBean(BeanDetails beanDetails) {
        this.lock.writeLock().lock();
        this.beans.put(beanDetails.getType(), beanDetails);
        this.lock.writeLock().unlock();

        return beanDetails;
    }

    public boolean isBean(Class<?> beanClass) {
        return this.beans.containsKey(beanClass);
    }

    public boolean isBean(Object bean) {
        return this.isBean(bean.getClass());
    }

}
