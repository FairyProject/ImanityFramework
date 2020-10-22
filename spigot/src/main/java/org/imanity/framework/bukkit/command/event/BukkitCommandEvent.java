package org.imanity.framework.bukkit.command.event;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.imanity.framework.command.CommandEvent;

public class BukkitCommandEvent extends CommandEvent {
    public BukkitCommandEvent(CommandSender user, String command) {
        super(user, command);
    }

    public CommandSender getSender() {
        return (CommandSender) this.getUser();
    }

    public Player getPlayer() {
        return (Player) this.getUser();
    }

    @Override
    public void sendUsage(String usage) {
        this.getSender().sendMessage(ChatColor.RED + "Usage: " + usage);
    }

    @Override
    public void sendError(Throwable throwable) {
        this.getSender().sendMessage(ChatColor.RED + "It appears there was some issues processing your command...");
    }

    @Override
    public void sendNoPermission() {
        this.getSender().sendMessage(ChatColor.RED + "No permission.");
    }

    @Override
    public void sendInternalError(String message) {
        this.getSender().sendMessage(ChatColor.RED + message);
    }
}
