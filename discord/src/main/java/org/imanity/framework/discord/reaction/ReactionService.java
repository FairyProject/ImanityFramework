package org.imanity.framework.discord.reaction;

import org.imanity.framework.Autowired;
import org.imanity.framework.PostInitialize;
import org.imanity.framework.PreInitialize;
import org.imanity.framework.Service;
import org.imanity.framework.discord.DiscordService;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(name = "reactionMessage", dependencies = "discord")
public class ReactionService {

    public static ReactionService INSTANCE;

    private Map<Long, ReactionMessage> messages;

    @Autowired
    private DiscordService discordService;

    @PreInitialize
    public void preInit() {
        INSTANCE = this;
    }

    @PostInitialize
    public void init() {
        this.messages = new ConcurrentHashMap<>();
    }

    @Nullable
    public ReactionMessage findMessage(long id) {
        return this.messages.getOrDefault(id, null);
    }

    public void send(ReactionMessage message) {
        this.messages.put(message.getMessage().getMessageId(), message);
    }

    public void delete(long id) {
        this.messages.remove(id);
    }

}
