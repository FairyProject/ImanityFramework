package org.imanity.framework.command;

public interface CommandProvider<T extends InternalCommandEvent> {
    
    boolean hasPermission(Object user, String permission);

    void sendUsage(T commandEvent, String usage);

    void sendError(T commandEvent, Throwable throwable);

    void sendNoPermission(T commandEvent);

    void sendInternalError(T commandEvent, String message);

    default boolean shouldExecute(T commandEvent, CommandMeta meta, String[] arguments) {
        return true;
    }

}
