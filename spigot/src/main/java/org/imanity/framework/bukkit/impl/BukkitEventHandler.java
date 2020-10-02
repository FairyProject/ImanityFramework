package org.imanity.framework.bukkit.impl;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.NetworkStateChangedEvent;
import org.imanity.framework.bukkit.events.PostServicesInitialEvent;
import org.imanity.framework.bukkit.listener.FunctionListener;
import org.imanity.framework.bukkit.reflection.resolver.ConstructorResolver;
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
    public void onPostServicesInitial() {
        Imanity.callEvent(new PostServicesInitialEvent());
    }
}
