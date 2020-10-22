package org.imanity.framework.discord.provider;

import net.dv8tion.jda.api.entities.Member;
import org.imanity.framework.discord.command.DiscordCommandEvent;

import javax.annotation.Nullable;

public class DiscordPresenceProvider {

    @Nullable
    public String prefix(Member member) {
        return "!";
    }

    public void sendUsage(DiscordCommandEvent event, String usage) {
        event.reply("Wrong Usage: " + usage);
    }

    public void sendError(DiscordCommandEvent event, Throwable exception) {
        event.reply("An error occurs: " + exception.getClass().getSimpleName() + " - " + exception.getLocalizedMessage());
    }

    public void sendNoPermission(DiscordCommandEvent event) {
        event.reply("No Permission!");
    }

    public void sendInternalError(DiscordCommandEvent event, String message) {
        event.reply("Internal Error: " + message);
    }

}
