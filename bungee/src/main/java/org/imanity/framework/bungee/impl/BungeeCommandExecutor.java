package org.imanity.framework.bungee.impl;

import org.imanity.framework.command.ICommandExecutor;
import org.imanity.framework.redis.server.ImanityServer;

public class BungeeCommandExecutor implements ICommandExecutor {
    @Override
    public void execute(String command, String context, String executor, ImanityServer server) {
        // TODO: events
    }
}
