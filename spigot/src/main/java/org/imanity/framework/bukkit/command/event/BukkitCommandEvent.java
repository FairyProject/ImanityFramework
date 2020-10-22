package org.imanity.framework.bukkit.command.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.command.InternalCommandEvent;

public class BukkitCommandEvent extends InternalCommandEvent {
    public BukkitCommandEvent(CommandSender user, String command) {
        super(user, command);
    }

    public CommandSender getSender() {
        return (CommandSender) this.getUser();
    }

    public Player getPlayer() {
        return (Player) this.getUser();
    }
}
