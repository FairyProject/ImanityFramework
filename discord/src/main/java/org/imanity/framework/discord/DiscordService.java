package org.imanity.framework.discord;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.command.InternalCommandEvent;
import org.imanity.framework.discord.activity.ActivityProvider;
import org.imanity.framework.discord.command.DiscordCommandEvent;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import javax.security.auth.login.LoginException;
import java.util.TreeSet;
import java.util.function.Function;

@Service(name = "discord")
@Getter
public class DiscordService implements IService {

    public static final Logger LOGGER = LogManager.getLogger(DiscordService.class);

    @Autowired
    private FrameworkBootable bootable;
    @Autowired
    private CommandService commandService;

    private TreeSet<ActivityProvider> activityProviders;

    private JDA jda;

    private Function<Member, @Nullable String> prefixProvider;

    @Override
    public void init() {
        LOGGER.info("Attempting to Login into discord...");

        this.activityProviders = new TreeSet<>((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
        this.prefixProvider = member -> "!";

        String token = this.bootable.get("discord.token", null);
        Preconditions.checkNotNull(token, "The token couldn't be found! please add [discord.token] into framework bootable configuration!");

        try {
            JDABuilder builder;

            if (this.bootable.getBoolean("discord.light", false)) {
                builder = JDABuilder.createLight(token);
            } else {
                builder = JDABuilder.createDefault(token);
            }

            this.jda = builder.build();
        } catch (LoginException e) {
            this.bootable.handleError(e);
        }

        int activityUpdateTicks = this.bootable.getInteger("discord.activityUpdateTicks", 20);
        this.bootable.getTaskScheduler().runAsyncRepeated(this::updateActivity, activityUpdateTicks);

        LOGGER.info("Logging into discord bot successful. discord: " + this.jda.getSelfUser().getName());
    }

    public void withPrefixProvider(Function<Member, @Nullable String> prefixProvider) {
        this.prefixProvider = prefixProvider;
    }

    private void updateActivity() {

        for (ActivityProvider activityProvider : this.activityProviders) {
            Activity activity = activityProvider.activity();

            if (activity != null) {
                Activity current = this.jda.getPresence().getActivity();
                if (current == null || !current.equals(activity)) {
                    this.jda.getPresence().setActivity(activity);
                }
                break;
            }
        }
    }
}
