package org.imanity.framework.discord.reaction;

import lombok.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.logging.log4j.util.TriConsumer;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.discord.DiscordService;
import org.imanity.framework.discord.message.CachedMessage;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;
import org.jetbrains.annotations.Nullable;
import org.mongojack.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Data
public class ReactionMessage {

    @Id
    private final CachedMessage message;

    private ReactionConsumer notRegisteredConsumer;
    private Map<String, ReactionConsumer> reactionConsumers = new HashMap<>();

    @Nullable
    public ReactionConsumer find(String emojiId) {
        return this.reactionConsumers.getOrDefault(emojiId, notRegisteredConsumer);
    }

    public CompletableFuture<Void> delete(boolean removeFromDiscord) {
        ImanityCommon.getService(ReactionService.class).delete(message.getMessageId());

        if (removeFromDiscord) {
            return this.message.delete();
        }

        return CompletableFuture.completedFuture(null);
    }

    @Nullable
    public RestAction<Message> getDiscordMessage() {
        return this.message.getMessage();
    }

    public void queueEmoji() {
        RestAction<Message> rest = this.getDiscordMessage();

        if (rest == null) {
            throw new IllegalArgumentException("Couldn't find the discord message!");
        }

        rest.queue(message -> {
                    for (ReactionConsumer consumer : this.reactionConsumers.values()) {
                        message.addReaction(consumer.getEmojiId()).queue();
                    }
                });
    }

    public TextChannel getChannel() {
        return this.message.getChannel();
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private CachedMessage message;
        private EntryArrayList<String, TriConsumer<User, ReactionMessage, MessageReactionAddEvent>> reactionConsumer = new EntryArrayList<>();
        private TriConsumer<User, ReactionMessage, MessageReactionAddEvent> notRegisteredConsumer;

        public Builder message(Message message) {
            this.message = new CachedMessage(message);
            return this;
        }

        public Builder message(CachedMessage message) {
            this.message = message;
            return this;
        }

        public Builder reaction(@NonNull Emote emote, @NonNull TriConsumer<User, ReactionMessage, MessageReactionAddEvent> consumer) {
            return this.reaction(emote.getName(), consumer);
        }

        public Builder reaction(@NonNull String emoteId, @NonNull TriConsumer<User, ReactionMessage, MessageReactionAddEvent> consumer) {
            this.reactionConsumer.add(emoteId, consumer);
            return this;
        }

        public Builder notRegisteredReaction(TriConsumer<User, ReactionMessage, MessageReactionAddEvent> consumer) {
            this.notRegisteredConsumer = consumer;
            return this;
        }

        public ReactionMessage handle() {
            ReactionMessage message = new ReactionMessage(this.message);

            for (Entry<String, TriConsumer<User, ReactionMessage, MessageReactionAddEvent>> entry : this.reactionConsumer) {
                message.getReactionConsumers().put(entry.getKey(), new ReactionConsumer(message, entry.getKey(), entry.getValue()));
            }

            if (this.notRegisteredConsumer != null) {
                message.setNotRegisteredConsumer(new ReactionConsumer(message, null, this.notRegisteredConsumer));
            }

            ReactionService.INSTANCE.send(message);
            return message;
        }

    }

}
