package org.imanity.framework.bukkit.impl;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.NetworkStateChangedEvent;
import org.imanity.framework.bukkit.listener.FunctionListener;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.util.reflection.resolver.ConstructorResolver;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.events.annotation.AutoWiredListener;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerState;

public class BukkitEventHandler implements IEventHandler {
    @Override
    public void onServerStateChanged(ImanityServer server, ServerState oldState, ServerState newState) {
        Imanity.callEvent(new NetworkStateChangedEvent(server, oldState, newState));
    }

    @Override
    public void registerWiredListener(String className) {
        try {
            Class<?> type = Class.forName(className);

            AutoWiredListener listenerAnnotation = type.getAnnotation(AutoWiredListener.class);
            if (listenerAnnotation == null) {
                return;
            }

            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(type);

            ConstructorResolver resolver = new ConstructorResolver(type);
            Object object = resolver.resolveWrapper(
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
                return;

            } else if (FunctionListener.class.isAssignableFrom(type)) {

                return;

            }

            Imanity.LOGGER.error("The Class " + type.getSimpleName() + " wasn't implement Listener or FunctionListener!");


        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while registering wired listener", throwable);
        }
    }
}
