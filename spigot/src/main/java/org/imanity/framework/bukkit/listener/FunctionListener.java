package org.imanity.framework.bukkit.listener;

import co.aikar.timings.TimedEventExecutor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FunctionListener<T extends Plugin> implements Listener {

    @Getter
    private FunctionEventChecker checker;
    public T plugin;

    public FunctionListener(FunctionEventChecker checker, T plugin) {
        this.checker = checker;
        this.plugin = plugin;
        this.init(plugin);
    }

    private void init(Plugin plugin) {
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<Class<? extends Event>, Set<RegisteredListener>>();
        Set<Method> methods;
        try {
            Method[] publicMethods = this.getClass().getMethods();
            Method[] privateMethods = this.getClass().getDeclaredMethods();
            methods = new HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
            for (Method method : publicMethods) {
                methods.add(method);
            }
            for (Method method : privateMethods) {
                methods.add(method);
            }
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + this.getClass() + " because " + e.getMessage() + " does not exist.");
            return;
        }

        for (Method method : methods) {
            FunctionEventHandler eventHandler = method.getAnnotation(FunctionEventHandler.class);
            if (eventHandler == null) continue;
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + this.getClass());
                continue;
            }
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.get(eventClass);
            if (eventSet == null) {
                eventSet = new HashSet<RegisteredListener>();
                ret.put(eventClass, eventSet);
            }

            EventExecutor executor = new TimedEventExecutor(new EventExecutor() {
                @Override
                public void execute(Listener listener, Event event) throws EventException {
                    try {
                        if (!eventClass.isAssignableFrom(event.getClass())) {
                            return;
                        }
                        if (!eventHandler.ignoreFunctionCheck() && !checker.check(event)) {
                            return;
                        }
                        method.invoke(listener, event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, plugin, method, eventClass);
            eventSet.add(new RegisteredListener(this, executor, eventHandler.priority(), plugin, eventHandler.ignoreCancelled()));
        }
        for (Map.Entry<Class<? extends Event>, Set<RegisteredListener>> entry : ret.entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
    }

    private static HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private static Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

}
