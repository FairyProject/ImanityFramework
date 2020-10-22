package org.imanity.framework.command;

public interface CommandProvider {
    
    boolean hasPermission(Object user, String permission);

    void sendUsage(InternalCommandEvent commandEvent, String usage);

    void sendError(InternalCommandEvent commandEvent, Exception exception);

    void sendNoPermission(InternalCommandEvent commandEvent);

    void sendInternalError(InternalCommandEvent commandEvent, String message);

    default boolean shouldExecute(InternalCommandEvent commandEvent, CommandMeta meta, String[] arguments) {
        return true;
    }

}
