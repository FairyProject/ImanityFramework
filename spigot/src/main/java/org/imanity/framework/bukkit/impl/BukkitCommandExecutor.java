package org.imanity.framework.bukkit.impl;

import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.NetworkCommandExecuteEvent;
import org.imanity.framework.command.ICommandExecutor;
import org.imanity.framework.redis.server.ImanityServer;

public class BukkitCommandExecutor implements ICommandExecutor {
    @Override
    public void execute(String command, String context, String executor, ImanityServer server) {
        NetworkCommandExecuteEvent event = new NetworkCommandExecuteEvent(server, command, context, executor);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        command = event.getCommand();
        Imanity.PLUGIN.getServer().dispatchCommand(Imanity.PLUGIN.getServer().getConsoleSender(), command);
    }
}
