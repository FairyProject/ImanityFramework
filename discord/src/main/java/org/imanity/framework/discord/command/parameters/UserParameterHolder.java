package org.imanity.framework.discord.command.parameters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.imanity.framework.Component;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.parameter.ParameterHolder;
import org.imanity.framework.discord.DiscordService;

import java.util.regex.Matcher;

@Component
public class UserParameterHolder implements ParameterHolder<User> {
    @Override
    public Class[] type() {
        return new Class[] {User.class};
    }

    @Override
    public User transform(CommandEvent commandEvent, String source) {
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(source);
        if (!matcher.find()) {
            return null;
        }

        return DiscordService.INSTANCE.getJda().getUserById(matcher.group(1));
    }
}
