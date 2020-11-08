package org.imanity.framework.discord.reaction;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.logging.log4j.util.TriConsumer;
import org.imanity.framework.discord.DiscordService;

@RequiredArgsConstructor
@Data
@Builder
public class ReactionConsumer {

    private final ReactionMessage message;

    private final String emojiId;
    private final TriConsumer<User, ReactionMessage, MessageReactionAddEvent> consumer;

    public Emote getEmote() {
        return DiscordService.INSTANCE.getJda().getEmoteById(emojiId);
    }

    public void handle(User user, MessageReactionAddEvent event) {
        this.consumer.accept(user, this.message, event);
    }

}
