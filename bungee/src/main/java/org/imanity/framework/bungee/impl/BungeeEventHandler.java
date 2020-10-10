package org.imanity.framework.bungee.impl;

import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.redis.server.ImanityServer;
import org.imanity.framework.redis.server.enums.ServerState;

// TODO: event
public class BungeeEventHandler implements IEventHandler {
    @Override
    public void onServerStateChanged(ImanityServer server, ServerState oldState, ServerState newState) {

    }

    @Override
    public void onPostServicesInitial() {

    }
}
