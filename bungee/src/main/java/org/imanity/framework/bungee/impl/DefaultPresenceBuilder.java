package org.imanity.framework.bungee.impl;

import org.imanity.framework.command.PresenceProvider;

// TODO
public class DefaultPresenceBuilder extends PresenceProvider<BungeeCommandEvent> {

    @Override
    public Class<BungeeCommandEvent> type() {
        return BungeeCommandEvent.class;
    }

    @Override
    public void sendUsage(BungeeCommandEvent event, String usage) {

    }

    @Override
    public void sendError(BungeeCommandEvent event, Throwable throwable) {

    }

    @Override
    public void sendNoPermission(BungeeCommandEvent event) {

    }

    @Override
    public void sendInternalError(BungeeCommandEvent event, String message) {

    }
}
