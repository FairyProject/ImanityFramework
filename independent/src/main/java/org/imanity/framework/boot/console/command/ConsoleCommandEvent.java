package org.imanity.framework.boot.console.command;

import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.util.CC;

public class ConsoleCommandEvent extends CommandEvent {

    public ConsoleCommandEvent(String command) {
        super(null, command);
    }

    @Override
    public String name() {
        return "console";
    }

    @Override
    public void sendError(Throwable throwable) {
        FrameworkBootable.LOGGER.error(CC.RED + "Some error occurs while executing your command", throwable);
    }

    @Override
    public void sendInternalError(String message) {
        FrameworkBootable.LOGGER.error(CC.RED + "Some error occurs while executing your command: " + message);
    }

    @Override
    public void sendNoPermission() {
        FrameworkBootable.LOGGER.error(CC.RED + "You have no permission.");
    }

    @Override
    public void sendUsage(String usage) {
        FrameworkBootable.LOGGER.error(CC.RED + "Wrong Usage: " + usage);
    }
}
