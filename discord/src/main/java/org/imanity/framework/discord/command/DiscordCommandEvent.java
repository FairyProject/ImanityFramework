package org.imanity.framework.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.imanity.framework.command.InternalCommandEvent;

public class DiscordCommandEvent extends InternalCommandEvent {

    @Getter
    private TextChannel channel;

    public DiscordCommandEvent(Member user, String command, TextChannel channel) {
        super(user, command);

        this.channel = channel;
    }

    public Member getMember() {
        return (Member) this.getUser();
    }
}
