package org.imanity.framework.bukkit.impl;

import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.NetworkStateChangedEvent;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerState;

public class BukkitEventHandler implements IEventHandler {
    @Override
    public void onServerStateChanged(ImanityServer server, ServerState oldState, ServerState newState) {
        Imanity.callEvent(new NetworkStateChangedEvent(server, oldState, newState));
    }
}
