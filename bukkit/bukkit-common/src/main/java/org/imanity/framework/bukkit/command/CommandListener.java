/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.command.event.BukkitCommandEvent;
import org.imanity.framework.bukkit.events.PostServicesInitialEvent;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.Component;
import org.imanity.framework.Autowired;
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
        CommandEvent commandEvent = new BukkitCommandEvent(event.getPlayer(), command);

        if (this.commandService.evalCommand(commandEvent)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        CommandEvent commandEvent = new BukkitCommandEvent(event.getSender(), event.getCommand());

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
