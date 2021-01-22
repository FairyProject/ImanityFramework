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

package org.imanity.framework.discord.reaction;

import lombok.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.logging.log4j.util.TriConsumer;
import org.imanity.framework.Async;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.discord.message.CachedMessage;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;
import org.jetbrains.annotations.Nullable;
import org.mongojack.Id;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Data
public class ReactionMessage {

    @Id
    private final CachedMessage message;
    private boolean enabled;

    private ReactionConsumer notRegisteredConsumer;
    private Map<String, ReactionConsumer> reactionConsumers = new LinkedHashMap<>();

    @Nullable
    public ReactionConsumer find(String emojiId) {
        return this.reactionConsumers.getOrDefault(emojiId, notRegisteredConsumer);
    }

    public CompletableFuture<Void> delete(boolean removeFromDiscord) {
        ImanityCommon.getBean(ReactionService.class).delete(message.getMessageId());
        this.enabled = false;

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

        rest.queue(this::addEmojis);
    }

    @Async
    private void addEmojis(Message message) {
        for (ReactionConsumer consumer : this.reactionConsumers.values()) {
            if (!this.enabled) {
                break;
            }
            message.addReaction(consumer.getEmojiId()).complete();
        }
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
            message.enabled = true;
            return message;
        }

    }

}
