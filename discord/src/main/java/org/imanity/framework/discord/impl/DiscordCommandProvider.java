package org.imanity.framework.discord.impl;

import org.imanity.framework.command.CommandMeta;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.discord.annotations.OnlyGuild;
import org.imanity.framework.discord.annotations.OnlyPrivate;
import org.imanity.framework.discord.command.DiscordCommandEvent;

public class DiscordCommandProvider implements CommandProvider<DiscordCommandEvent> {
    @Override
    public boolean hasPermission(Object user, String permission) {
        return true;
    }

    @Override
    public void sendUsage(DiscordCommandEvent commandEvent, String usage) {
        commandEvent.reply("Wrong Usage: " + usage);
    }

    @Override
    public void sendError(DiscordCommandEvent commandEvent, Exception exception) {
        commandEvent.reply("An error occurs: " + exception.getClass().getSimpleName() + " - " + exception.getLocalizedMessage());
    }

    @Override
    public void sendNoPermission(DiscordCommandEvent commandEvent) {
        commandEvent.reply("No Permission!");
    }

    @Override
    public void sendInternalError(DiscordCommandEvent commandEvent, String message) {
        commandEvent.reply("Internal Error: " + message);
    }

    @Override
    public boolean shouldExecute(DiscordCommandEvent commandEvent, CommandMeta meta, String[] arguments) {
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
