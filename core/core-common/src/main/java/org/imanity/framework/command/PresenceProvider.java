package org.imanity.framework.command;

public abstract class PresenceProvider<T extends CommandEvent> {

    final void sendUsage0(CommandEvent event, String usage) {
        this.sendUsage((T) event, usage);
    }

    final void sendError0(CommandEvent event, Throwable throwable) {
        this.sendError((T) event, throwable);
    }

    final void sendNoPermission0(CommandEvent event) {
        this.sendNoPermission((T) event);
    }

    final void sendInternalError0(CommandEvent event, String message) {
        this.sendInternalError((T) event, message);
    }

    public abstract Class<T> type();

    public abstract void sendUsage(T event, String usage);

    public abstract void sendError(T event, Throwable throwable);

    public abstract void sendNoPermission(T event);

    public abstract void sendInternalError(T event, String message);

}
