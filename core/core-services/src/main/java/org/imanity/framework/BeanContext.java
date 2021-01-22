/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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
import org.imanity.framework.details.*;
import org.imanity.framework.exception.ServiceAlreadyExistsException;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginListenerAdapter;
import org.imanity.framework.plugin.PluginManager;
import org.imanity.framework.reflect.Reflect;
import org.imanity.framework.reflect.ReflectLookup;
import org.imanity.framework.util.AccessUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BeanContext {

    public static boolean SHOW_LOGS = false;

    public static BeanContext INSTANCE;

    protected static final Logger LOGGER = LogManager.getLogger(BeanContext.class);
    protected static void log(String msg) {
        if (SHOW_LOGS) {
            LOGGER.info("[BeanContext] " + msg);
        }
    }

    private final Map<Class<?>, BeanDetails> beanByType = new ConcurrentHashMap<>();
    private final Map<String, BeanDetails> beanByName = new ConcurrentHashMap<>();

    /**
     * NOT THREAD SAFE
     */
    private final List<BeanDetails> sortedBeans = new ArrayList<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public BeanDetails getBeanDetails(Class<?> type) {
        return this.beanByType.get(type);
    }

    public Object getBean(@NonNull Class<?> type) {
        BeanDetails details = this.getBeanDetails(type);
        if (details == null) {
            return null;
        }
        return details.getInstance();
    }

    public BeanDetails getBeanByName(String name) {
        return this.beanByName.get(name);
    }

    public BeanDetails[] getBeans() {
        return this.beanByType.values().toArray(new BeanDetails[0]);
    }

    private List<String> findClassPaths(Class<?> plugin) {
        ClasspathScan annotation = plugin.getAnnotation(ClasspathScan.class);

        if (annotation != null) {
            return Lists.newArrayList(annotation.value());
        }

        return Collections.emptyList();
    }

    public Collection<BeanDetails> findDetailsBindWith(AbstractPlugin plugin) {
        return this.beanByType.values()
                .stream()
                .filter(beanDetails -> beanDetails.isBind() && beanDetails.getBindPlugin().equals(plugin))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void injectBeans(Object instance) {
        Collection<Field> fields = Reflect.getDeclaredFields(instance.getClass());

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            Autowired annotation = field.getAnnotation(Autowired.class);

            if (annotation == null || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }

            Object service = this.getBean(field.getType());

            if (service != null) {
                AccessUtil.setAccessible(field);
                Reflect.setField(instance, field, service);
            } else {
                throw new IllegalArgumentException("Couldn't find bean " + field.getType().getName() + " !");
            }
        }
    }

    @SneakyThrows
    public ComponentBeanDetails registerComponent(Object instance, Class<?> type, ComponentHolder componentHolder) {
        Component annotation = type.getAnnotation(Component.class);
        if (annotation == null) {
            throw new IllegalArgumentException("The type " + type.getName() + " doesn't have Component annotation!");
        }

        String name = annotation.value();
        if (name.length() == 0) {
            name = instance.getClass().getName();
        }

        ComponentBeanDetails details = new ComponentBeanDetails(type, instance, name, componentHolder);
        this.registerBean(details);
        this.attemptBindPlugin(details);

        details.call(PreInitialize.class);
//        this.injectBeans(instance); // put into BeanContext

        return details;
    }

    private void attemptBindPlugin(BeanDetails beanDetails) {
        if (PluginManager.isInitialized()) {
            AbstractPlugin plugin = PluginManager.INSTANCE.getPluginByClass(beanDetails.getType());

            if (plugin != null) {
                beanDetails.bindWith(plugin);

                log("Bean " + beanDetails.getName() + " is now bind with plugin " + plugin.getName());
            }
        }
    }

    public void scanClasses(String scanName, ClassLoader classLoader, Collection<String> classPaths, BeanDetails... included) throws Exception {

        long start = System.currentTimeMillis();

        log("Start scanning beans for " + scanName + " with packages [" + String.join(" ", classPaths) + "]...");

        ReflectLookup reflectLookup = new ReflectLookup(Collections.singleton(classLoader), classPaths);
        log("Finish build Reflect Lookup instance with in " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        List<BeanDetails> beanDetailsList = new ArrayList<>(Arrays.asList(included));

        for (Class<?> type : reflectLookup.findAnnotatedClasses(Service.class)) {

            Service service = type.getAnnotation(Service.class);
            Preconditions.checkNotNull(service, "The type " + type.getName() + " doesn't have @Service annotation!");

            String name = service.name();

            if (this.getBeanByName(name) == null) {
                ServiceBeanDetails beanDetails = new ServiceBeanDetails(type, name, service.dependencies());

                log("Found " + name + " with type " + type.getSimpleName() + ", Registering it as bean...");

                this.attemptBindPlugin(beanDetails);
                this.registerBean(beanDetails, false);

                beanDetailsList.add(beanDetails);
            } else {
                new ServiceAlreadyExistsException(name).printStackTrace();
            }
        }

        log("Finish scanning beans within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        beanDetailsList = this.loadInOrder(beanDetailsList);
        log("Finish initialize beans in order within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        this.sortedBeans.addAll(beanDetailsList);

        beanDetailsList.forEach(beanDetails -> {
            try {
                if (!beanDetails.shouldInitialize()) {
                    this.unregisterBean(beanDetails);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                LOGGER.error(e);
                this.unregisterBean(beanDetails);
            }
        });
        log("Unregistered shouldn't initialized beans within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        this.call(PreInitialize.class, beanDetailsList);
        log("Finish pre enable beans within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        beanDetailsList.addAll(ComponentRegistry.scanComponents(this, reflectLookup));
        log("Finish scanning component " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        beanDetailsList.forEach(beanDetails -> {
            Object instance = beanDetails.getInstance();
            if (instance != null) {
                this.injectBeans(instance);
            }
        });
        log("Finish injecting beans within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        for (Field field : reflectLookup.findAnnotatedStaticFields(Autowired.class)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            AccessUtil.setAccessible(field);

            Object bean = this.getBean(field.getType());
            Reflect.setField(null, field, bean);
        }
        log("Finish injecting static fields within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        beanDetailsList.forEach(BeanDetails::onEnable);
        log("Finish call onEnable() within " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();

        this.call(PostInitialize.class, beanDetailsList);
        log("Finish post initalize " + (System.currentTimeMillis() - start) + "ms, scanning for " + scanName + " finished.");

    }

    @SneakyThrows
    public void init() {
        INSTANCE = this;

        this.registerBean(new SimpleBeanDetails(this, "beanContext", this.getClass()));
        log("BeanContext has been registered as bean.");

        ComponentRegistry.registerComponentHolders();
        this.scanClasses("framework", BeanContext.class.getClassLoader(), Collections.singleton("org.imanity.framework"));

        if (PluginManager.isInitialized()) {
            log("Find PluginManager, attempt to register Plugin Listeners");

            PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
                @Override
                public void onPluginEnable(AbstractPlugin plugin) {
                    BeanDetails beanDetails = new SimpleBeanDetails(plugin, plugin.getName(), plugin.getClass());

                    beanDetails.bindWith(plugin);
                    registerBean(beanDetails, false);
                    log("Plugin " + plugin.getName() + " has been registered as bean.");

                    try {
                        scanClasses(plugin.getName(), plugin.getPluginClassLoader(), findClassPaths(plugin.getClass()), beanDetails);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable);
                    }
                }

                @SneakyThrows
                @Override
                public void onPluginDisable(AbstractPlugin plugin) {
                    Collection<BeanDetails> beanDetailsList = findDetailsBindWith(plugin);
                    try {
                        call(PreDestroy.class, beanDetailsList);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable);
                    }

                    beanDetailsList.forEach(details -> {
                        log("Bean " + details.getName() + " Disabled, due to plugin " + plugin.getName() + " disabled.");

                        try {
                            details.onDisable();
                            unregisterBean(details);
                        } catch (Throwable throwable) {
                            LOGGER.error(throwable);
                        }
                    });

                    try {
                        call(PostDestroy.class, beanDetailsList);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable);
                    }
                }
            });
        }

        FrameworkMisc.EVENT_HANDLER.onPostServicesInitial();
    }

    @SneakyThrows
    public void stop() {
        List<BeanDetails> detailsList = Lists.newArrayList(this.sortedBeans);
        Collections.reverse(detailsList);

        this.call(PreDestroy.class, detailsList);

        for (BeanDetails details : detailsList) {
            log("Bean " + details.getName() + " Disabled, due to framework being disabled.");

            details.onDisable();
            unregisterBean(details);
        }

        this.call(PostDestroy.class, detailsList);
    }

    public void call(Class<? extends Annotation> annotation, Collection<BeanDetails> beanDetailsList) throws InvocationTargetException, IllegalAccessException {
        for (BeanDetails beanDetails : beanDetailsList) {
            beanDetails.call(annotation);
        }
    }

    private List<BeanDetails> loadInOrder(List<BeanDetails> beanDetailsList) {
        Map<String, BeanDetails> unloaded = new HashMap<>();
        for (BeanDetails beanDetails : beanDetailsList) {
            unloaded.put(beanDetails.getName(), beanDetails);

            if (beanDetails instanceof ServiceBeanDetails) {
                ((ServiceBeanDetails) beanDetails).setupConstruction(this);
            }
        }

        // Remove Services without valid dependency
        Iterator<Map.Entry<String, BeanDetails>> removeIterator = unloaded.entrySet().iterator();
        while (removeIterator.hasNext()) {
            Map.Entry<String, BeanDetails> entry = removeIterator.next();
            BeanDetails beanDetails = entry.getValue();

            if (!(beanDetails instanceof ServiceBeanDetails) || !((ServiceBeanDetails) beanDetails).hasDependencies()) {
                continue;
            }

            ServiceBeanDetails serviceBeanDetails = (ServiceBeanDetails) beanDetails;

            for (String dependency : serviceBeanDetails.getDependencies()) {
                BeanDetails dependencyDetails = this.getBeanByName(dependency);

                if (dependencyDetails == null) {
                    LOGGER.error("Couldn't find the dependency " + dependency + " for " + serviceBeanDetails.getName() + "!");
                    removeIterator.remove();
                    break;
                } else {

                    // Prevent dependency each other
                    if (dependencyDetails instanceof ServiceBeanDetails
                                    && ((ServiceBeanDetails) dependencyDetails).hasDependencies()
                                    && ((ServiceBeanDetails) dependencyDetails).getDependencies().contains(serviceBeanDetails.getName())) {
                        LOGGER.error("Target " + serviceBeanDetails.getName() + " and " + dependency + " depend to each other!");
                        removeIterator.remove();

                        unloaded.remove(dependency);
                        break;
                    }
                }
            }
        }

        // Continually loop until all dependency found and loaded
        List<BeanDetails> sorted = new ArrayList<>();

        while (!unloaded.isEmpty()) {
            Iterator<Map.Entry<String, BeanDetails>> iterator = unloaded.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, BeanDetails> entry = iterator.next();
                BeanDetails beanDetails = entry.getValue();
                boolean missingDependencies = true;

                if (!(beanDetails instanceof ServiceBeanDetails) || !((ServiceBeanDetails) beanDetails).hasDependencies()) {
                    missingDependencies = false;
                } else {
                    List<String> list = Lists.newArrayList(((ServiceBeanDetails) beanDetails).getDependencies());
                    list.removeIf(dependency -> {
                        BeanDetails dependencyDetails = this.getBeanByName(dependency);
                        return dependencyDetails != null && dependencyDetails.getInstance() != null;
                    });

                    if (list.isEmpty()) {
                        missingDependencies = false;
                    }
                }

                if (!missingDependencies) {
                    if (beanDetails instanceof ServiceBeanDetails) {
                        ((ServiceBeanDetails) beanDetails).build(this);
                    }

                    sorted.add(beanDetails);
                    iterator.remove();
                }
            }
        }

        return sorted;
    }

    public BeanDetails registerBean(BeanDetails beanDetails) {
        return this.registerBean(beanDetails, true);
    }

    public BeanDetails registerBean(BeanDetails beanDetails, boolean sort) {
        this.beanByType.put(beanDetails.getType(), beanDetails);
        this.beanByName.put(beanDetails.getName(), beanDetails);
        if (sort) {
            this.sortedBeans.add(beanDetails);
        }

        return beanDetails;
    }

    public void unregisterBean(Class<?> type) {
        this.unregisterBean(this.getBeanDetails(type));
    }

    public void unregisterBean(String name) {
        this.unregisterBean(this.getBeanByName(name));
    }

    // UNFINISHED, or finished? idk
    public void unregisterBean(@NonNull BeanDetails beanDetails) {
        this.beanByType.remove(beanDetails.getType());
        this.beanByName.remove(beanDetails.getName());

        this.lock.writeLock().lock();
        this.sortedBeans.remove(beanDetails);
        this.lock.writeLock().unlock();
    }

    public boolean isBean(Class<?> beanClass) {
        return this.beanByType.containsKey(beanClass);
    }

    public boolean isBean(Object bean) {
        return this.isBean(bean.getClass());
    }

}
