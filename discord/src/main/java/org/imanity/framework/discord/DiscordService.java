/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.discord;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.discord.provider.DiscordBotProvider;
import org.jetbrains.annotations.Nullable;
import org.imanity.framework.*;
import org.imanity.framework.boot.FrameworkBootable;
import org.imanity.framework.command.CommandProvider;
import org.imanity.framework.command.CommandService;
import org.imanity.framework.discord.activity.ActivityProvider;
import org.imanity.framework.discord.command.DiscordCommandEvent;
import org.imanity.framework.discord.impl.DiscordListenerComponentHolder;
import org.imanity.framework.discord.provider.DiscordPresenceProvider;

import javax.security.auth.login.LoginException;
import java.util.*;

@Service(name = "discord")
@Getter
public class DiscordService {

    public static final Logger LOGGER = LogManager.getLogger(DiscordService.class);
    public static DiscordService INSTANCE;

    public static final String
            TOKEN = "discord.token",
            LIGHT = "discord.light",
            GUILD = "discord.guild",
            ACTIVITY_UPDATE_TICKS = "discord.activityUpdateTicks",
            USE_DEFAULT_COMMAND_PROVIDER = "discord.command.useDefaultProvider";

    @Autowired
    private FrameworkBootable bootable;
    @Autowired
    private CommandService commandService;

    private TreeSet<ActivityProvider> activityProviders;
    private List<ListenerAdapter> listenerAdapters;
    private List<DiscordBotProvider> botProviders;

    private DiscordPresenceProvider presenceProvider;

    private long guildId;

    private JDA jda;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;

        this.listenerAdapters = new ArrayList<>();
        this.botProviders = new ArrayList<>();
        this.activityProviders = new TreeSet<>((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));

        ComponentRegistry.registerComponentHolder(new DiscordListenerComponentHolder());
        ComponentRegistry.registerComponentHolder(new ComponentHolder() {
            @Override
            public Class<?>[] type() {
                return new Class[] { DiscordBotProvider.class };
            }

            @Override
            public Object newInstance(Class<?> type) {
                DiscordBotProvider provider = (DiscordBotProvider) super.newInstance(type);

                botProviders.add(provider);
                return provider;
            }
        });

    }

    @PostInitialize
    public void init() {
        LOGGER.info("Attempting to Login into discord...");

        if (this.presenceProvider == null) {
            this.withPresenceProvider(new DiscordPresenceProvider());
        }

        String token = this.bootable.get(TOKEN, null);
        Preconditions.checkNotNull(token, "The token couldn't be found! please add [discord.token] into framework bootable configuration!");

        try {
            JDABuilder builder;

            if (this.bootable.getBoolean(LIGHT, false)) {
                builder = JDABuilder.createLight(token);
            } else {
                builder = JDABuilder.createDefault(token);
            }

            builder
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableIntents(EnumSet.allOf(GatewayIntent.class));

            for (DiscordBotProvider botProvider : this.botProviders) {
                botProvider.setupBot(builder);
            }
            this.botProviders.clear();
            this.botProviders = null;

            for (ListenerAdapter adapter : this.listenerAdapters) {
                builder.addEventListeners(adapter);
            }
            this.listenerAdapters.clear();
            this.listenerAdapters = null;

            this.jda = builder.build();
            this.jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            this.bootable.handleError(e);
        }

        this.guildId = this.bootable.getLong(GUILD, -1);

        if (this.bootable.getBoolean(USE_DEFAULT_COMMAND_PROVIDER, true)) {
            this.commandService.withProvider(new CommandProvider() {
                @Override
                public boolean hasPermission(Object user, String permission) {
                    return true;
                }
            });
        }

        int activityUpdateTicks = this.bootable.getInteger(ACTIVITY_UPDATE_TICKS, 20);
        this.bootable.getTaskScheduler().runAsyncRepeated(this::updateActivity, activityUpdateTicks);

        SelfUser user = this.jda.getSelfUser();
        LOGGER.info("Logging into discord bot successful. Discord Tag: " + user.getAsTag());
    }

    @PostDestroy
    public void destroy() {
        this.jda.shutdown();
    }

    public boolean isLoggedIn() {
        return this.jda != null;
    }

    @Nullable
    public Guild getGuild() {
        Preconditions.checkArgument(this.guildId != -1, "The Guild ID hasn't been set!");
        return this.jda.getGuildById(this.guildId);
    }

    @Nullable
    public Member getMemberById(long id) {
        Guild guild = this.getGuild();
        Preconditions.checkNotNull(guild, "The Guild is null!");

        return guild.getMemberById(id);
    }

    public void withPresenceProvider(DiscordPresenceProvider presenceProvider) {
        this.presenceProvider = presenceProvider;
        this.commandService.registerDefaultPresenceProvider(presenceProvider);
    }

    public void registerListener(ListenerAdapter listener) {
        if (this.isLoggedIn()) {
            this.jda.addEventListener(listener);
        } else {
            this.listenerAdapters.add(listener);
        }
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

    public void handleMessageReceived(Member member, Message message, MessageChannel channel) {
        String rawMessage = message.getContentRaw();

        String prefix = this.getPresenceProvider().prefix(member);

        // Disable if prefix is null for length is 0
        if (prefix == null || prefix.length() == 0) {
            return;
        }

        // Doesn't match to prefix
        if (!rawMessage.startsWith(prefix)) {
            return;
        }

        DiscordCommandEvent commandEvent = new DiscordCommandEvent(member, rawMessage.substring(1), channel, message);
        commandService.evalCommand(commandEvent);
    }
}
