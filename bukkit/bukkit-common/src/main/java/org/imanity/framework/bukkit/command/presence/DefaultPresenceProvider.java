package org.imanity.framework.bukkit.command.presence;

import org.bukkit.ChatColor;
import org.imanity.framework.bukkit.command.event.BukkitCommandEvent;
import org.imanity.framework.command.PresenceProvider;

public class DefaultPresenceProvider extends PresenceProvider<BukkitCommandEvent> {

    @Override
    public Class<BukkitCommandEvent> type() {
        return BukkitCommandEvent.class;
    }

    @Override
    public void sendUsage(BukkitCommandEvent event, String usage) {
        event.getSender().sendMessage(ChatColor.RED + "Usage: " + usage);
    }

    @Override
    public void sendError(BukkitCommandEvent event, Throwable throwable) {
        event.getSender().sendMessage(ChatColor.RED + "It appears there was some issues processing your command...");
    }

    @Override
    public void sendNoPermission(BukkitCommandEvent event) {
        event.getSender().sendMessage(ChatColor.RED + "No permission.");
    }

    @Override
    public void sendInternalError(BukkitCommandEvent event, String message) {
        event.getSender().sendMessage(ChatColor.RED + message);
    }
}
