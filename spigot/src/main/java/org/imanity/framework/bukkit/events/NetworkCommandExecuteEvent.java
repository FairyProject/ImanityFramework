package org.imanity.framework.bukkit.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.imanity.framework.redis.server.ImanityServer;

@Getter
public class NetworkCommandExecuteEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final ImanityServer sender;
    @Setter private String command;
    private final String context;
    private final String executor;
    @Setter private boolean cancelled;

    public NetworkCommandExecuteEvent(ImanityServer sender, String command, String context, String executor) {
        this.sender = sender;
        this.command = command;
        this.context = context;
        this.executor = executor;
    }

}
