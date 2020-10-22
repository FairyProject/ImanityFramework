package org.imanity.framework.discord.listener;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
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

        Member member = event.getMember();

        if (member.getIdLong() == discordService.getJda().getSelfUser().getIdLong()) {
            return;
        }

        Message message = event.getMessage();
        String rawMessage = message.getContentRaw();

        String prefix = this.discordService.getPrefixProvider().apply(member);

        // Disable if prefix is null for length is 0
        if (prefix == null || prefix.length() == 0) {
            return;
        }


        // Doesn't match to prefix
        if (!rawMessage.startsWith(prefix)) {
            return;
        }

        DiscordCommandEvent commandEvent = new DiscordCommandEvent(member, rawMessage.substring(1), event.getChannel());
        commandService.evalCommand(commandEvent);

    }
}
