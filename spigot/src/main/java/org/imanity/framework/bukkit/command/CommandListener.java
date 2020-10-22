package org.imanity.framework.bukkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.command.event.BukkitCommandEvent;
import org.imanity.framework.bukkit.events.PostServicesInitialEvent;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.util.AccessUtil;

import java.lang.reflect.Field;

@Component
public class CommandListener implements Listener {

    @Autowired
    private CommandService commandService;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1);

        CommandMap.parameters.put(event.getPlayer().getUniqueId(), command.split(" "));
        InternalCommandEvent commandEvent = new BukkitCommandEvent(event.getPlayer(), command);

        if (this.commandService.evalCommand(commandEvent)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        InternalCommandEvent commandEvent = new BukkitCommandEvent(event.getSender(), event.getCommand());

        if (this.commandService.evalCommand(commandEvent)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPostServiceInitial(PostServicesInitialEvent event) {
        this.commandService.withProvider(new CommandProvider() {
            @Override
            public boolean hasPermission(Object user, String permission) {
                if (user instanceof CommandSender) {
                    return ((CommandSender) user).hasPermission(permission);
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendUsage(InternalCommandEvent commandEvent, String usage) {
                if (commandEvent.getUser() instanceof CommandSender) {
                    ((CommandSender) commandEvent.getUser()).sendMessage(ChatColor.RED + "Usage: " + usage);
                    return;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendError(InternalCommandEvent commandEvent, Throwable throwable) {
                if (commandEvent.getUser() instanceof CommandSender) {
                    ((CommandSender) commandEvent.getUser()).sendMessage(ChatColor.RED + "It appears there was some issues processing your command...");
                    return;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendNoPermission(InternalCommandEvent commandEvent) {
                if (commandEvent.getUser() instanceof CommandSender) {
                    ((CommandSender) commandEvent.getUser()).sendMessage(ChatColor.RED + "No permission.");
                    return;
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendInternalError(InternalCommandEvent commandEvent, String message) {
                if (commandEvent.getUser() instanceof CommandSender) {
                    ((CommandSender) commandEvent.getUser()).sendMessage(ChatColor.RED + message);
                    return;
                }
                throw new UnsupportedOperationException();
            }
        });

        TaskUtil.runScheduled(() -> {
            try {
                // Command map field (we have to use reflection to get this)
                final Field commandMapField = Imanity.PLUGIN.getServer().getClass().getDeclaredField("commandMap");
                AccessUtil.setAccessible(commandMapField);

                final Object oldCommandMap = commandMapField.get(Imanity.PLUGIN.getServer());
                final CommandMap newCommandMap = new CommandMap(Imanity.PLUGIN.getServer());

                // Start copying the knownCommands field over
                // (so any commands registered before we hook in are kept)
                final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");

                // The knownCommands field is final,
                // so to be able to set it in the new command map we have to remove it.
                AccessUtil.setAccessible(knownCommandsField);

                knownCommandsField.set(newCommandMap, knownCommandsField.get(oldCommandMap));
                // End copying the knownCommands field over

                commandMapField.set(Imanity.PLUGIN.getServer(), newCommandMap);
            } catch (final Exception e) {
                // Shouldn't happen, so we can just
                // printout the exception (and do nothing else)
            }
        }, 5L);
    }

}
