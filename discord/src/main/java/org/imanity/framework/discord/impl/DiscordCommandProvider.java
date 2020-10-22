package org.imanity.framework.discord.impl;

import org.imanity.framework.command.CommandMeta;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.discord.annotations.OnlyGuild;
import org.imanity.framework.discord.annotations.OnlyPrivate;
import org.imanity.framework.discord.command.DiscordCommandEvent;

public class DiscordCommandProvider implements CommandProvider {
    @Override
    public boolean hasPermission(Object user, String permission) {
        return true;
    }

    @Override
    public final void sendUsage(InternalCommandEvent commandEvent, String usage) {
        this.sendUsage((DiscordCommandEvent) commandEvent, usage);
    }

    @Override
    public final void sendError(InternalCommandEvent commandEvent, Exception exception) {
        this.sendError((DiscordCommandEvent) commandEvent, exception);
    }

    @Override
    public final void sendNoPermission(InternalCommandEvent commandEvent) {
        this.sendNoPermission((DiscordCommandEvent) commandEvent);
    }

    @Override
    public final void sendInternalError(InternalCommandEvent commandEvent, String message) {
        this.sendInternalError((DiscordCommandEvent) commandEvent, message);
    }

    public void sendUsage(DiscordCommandEvent commandEvent, String usage) {
        commandEvent.reply("Wrong Usage: " + usage);
    }

    public void sendError(DiscordCommandEvent commandEvent, Exception exception) {
        commandEvent.reply("An error occurs: " + exception.getClass().getSimpleName() + " - " + exception.getLocalizedMessage());
    }

    public void sendNoPermission(DiscordCommandEvent commandEvent) {
        commandEvent.reply("No Permission!");
    }

    public void sendInternalError(DiscordCommandEvent commandEvent, String message) {
        commandEvent.reply("Internal Error: " + message);
    }

    @Override
    public boolean shouldExecute(InternalCommandEvent commandEvent, CommandMeta meta, String[] arguments) {
        DiscordCommandEvent event = (DiscordCommandEvent) commandEvent;

        if (meta.getMethod().getAnnotation(OnlyPrivate.class) != null && !event.isPrivate()) {
            return false;
        }

        if (meta.getMethod().getAnnotation(OnlyGuild.class) != null && !event.isGuild()) {
            return false;
        }

        return true;
    }
}
