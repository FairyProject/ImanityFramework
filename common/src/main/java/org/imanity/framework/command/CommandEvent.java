package org.imanity.framework.command;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class CommandEvent {

    @Nullable
    private final Object user;

    private final String command;

    public CommandEvent(@Nullable Object user, String command) {
        this.user = user;
        this.command = command;
    }

    public String name() {
        return "default-executor";
    }

    public <T extends CommandEvent> T cast(Class<T> type) {
        if (!type.isInstance(this)) {
            throw new UnsupportedOperationException();
        }
        return (T) this;
    }

    public void sendUsage(String usage) {

    }

    public void sendError(Throwable throwable) {

    }

    public void sendNoPermission() {

    }

    public void sendInternalError(String message) {

    }

    public boolean shouldExecute(CommandMeta meta, String[] arguments) {
        return true;
    }

}
