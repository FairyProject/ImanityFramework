package org.imanity.framework.boot.console.command;

import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.command.PresenceProvider;
import org.imanity.framework.util.CC;

public class ConsolePresenceProvider extends PresenceProvider<ConsoleCommandEvent> {

    @Override
    public Class<ConsoleCommandEvent> type() {
        return ConsoleCommandEvent.class;
    }

    @Override
    public void sendError(ConsoleCommandEvent event, Throwable throwable) {
        FrameworkBootable.LOGGER.error(CC.RED + "Some error occurs while executing your command", throwable);
    }

    @Override
    public void sendInternalError(ConsoleCommandEvent event, String message) {
        FrameworkBootable.LOGGER.error(CC.RED + "Some error occurs while executing your command: " + message);
    }

    @Override
    public void sendNoPermission(ConsoleCommandEvent event) {
        FrameworkBootable.LOGGER.error(CC.RED + "You have no permission.");
    }

    @Override
    public void sendUsage(ConsoleCommandEvent event, String usage) {
        FrameworkBootable.LOGGER.error(CC.RED + "Wrong Usage: " + usage);
    }

}
