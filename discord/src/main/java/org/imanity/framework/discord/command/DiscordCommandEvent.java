package org.imanity.framework.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

    public void reply(MessageEmbed embed) {
        this.channel.sendMessage(embed).queue();
    }
}
