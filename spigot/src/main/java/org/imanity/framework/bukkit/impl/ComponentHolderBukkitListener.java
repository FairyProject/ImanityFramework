package org.imanity.framework.bukkit.impl;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.FunctionListener;
import org.imanity.framework.bukkit.reflection.resolver.ConstructorResolver;
import org.imanity.framework.plugin.component.ComponentHolder;

public class ComponentHolderBukkitListener extends ComponentHolder {

    @Override
    public Object newInstance(Class<?> type) {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(type);

        ConstructorResolver resolver = new ConstructorResolver(type);
        Object object = resolver.resolveMatches(
                new Class[0],
                new Class[] {plugin.getClass()},
                new Class[] {JavaPlugin.class}
        )
                .resolveBunch(
                        new Object[0],
                        new Object[] { plugin }
                );

        if (Listener.class.isAssignableFrom(type)) {

            Imanity.registerEvents((Listener) object);

        } else if (!FunctionListener.class.isAssignableFrom(type)) {
            Imanity.LOGGER.error("The Class " + type.getSimpleName() + " wasn't implement Listener or FunctionListener!");

            return null;
        }

        return object;
    }

    @Override
    public Class<?>[] type() {
        return new Class[] {FunctionListener.class, Listener.class};
    }
}
