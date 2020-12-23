package org.imanity.framework.bungee.impl;

import org.imanity.framework.command.CommandEvent;

public class BungeeCommandEvent extends CommandEvent {
    public BungeeCommandEvent(Object user, String command) {
        super(user, command);
    }
}
