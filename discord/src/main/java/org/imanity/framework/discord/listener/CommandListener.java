package org.imanity.framework.discord.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.discord.DiscordService;
import org.imanity.framework.discord.command.DiscordCommandEvent;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;
import org.jetbrains.annotations.NotNull;

@Component
public class CommandListener extends ListenerAdapter {

    @Autowired
    private DiscordService discordService;
    @Autowired
    private CommandService commandService;

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Member member = event.getMember();

        if (member == null) {
            return;
        }

        this.discordService.handleMessageReceived(member, event.getMessage(), event.getChannel());

    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {

        User author = event.getAuthor();

        if (author.isBot()) {
            return;
        }

        Member member = this.discordService.getMemberById(author.getIdLong());
        if (member == null) {
            return;
        }

        this.discordService.handleMessageReceived(member, event.getMessage(), event.getChannel());

    }
}
