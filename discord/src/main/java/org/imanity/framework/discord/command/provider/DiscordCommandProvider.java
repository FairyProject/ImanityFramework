package org.imanity.framework.discord.command.provider;

import net.dv8tion.jda.api.entities.User;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.discord.command.DiscordCommandEvent;

public interface DiscordCommandProvider extends CommandProvider<DiscordCommandEvent> {
    @Override
    default boolean hasPermission(Object user, String permission) {
        return false;
    }

    boolean hasPermission(User user, String permission);
}
