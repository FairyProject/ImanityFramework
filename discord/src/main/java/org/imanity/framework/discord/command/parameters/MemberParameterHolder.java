package org.imanity.framework.discord.command.parameters;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.imanity.framework.Component;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.discord.DiscordService;

import java.util.regex.Matcher;

@Component
public class MemberParameterHolder implements ParameterHolder<Member> {

    @Override
    public Class[] type() {
        return new Class[] {Member.class};
    }

    @Override
    public Member transform(CommandEvent commandEvent, String source) {
        Guild guild = DiscordService.INSTANCE.getGuild();
        if (guild == null) {
            return null;
        }

        Matcher matcher = Message.MentionType.USER.getPattern().matcher(source);
        if (!matcher.find()) {
            return null;
        }
        long id = Long.parseLong(matcher.group(1));

        return guild.getMemberById(id);
    }
}
