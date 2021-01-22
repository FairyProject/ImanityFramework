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

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.imanity.framework.Autowired;
import org.imanity.framework.Component;
import org.jetbrains.annotations.NotNull;

@Component
public class ReactionListener extends ListenerAdapter {

    @Autowired
    private ReactionService reactionService;

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null) {
            return;
        }

        if (user.isBot()) {
            return;
        }

        long id = event.getMessageIdLong();
        String emojiId = event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : null;
        ReactionMessage reactionMessage = this.reactionService.findMessage(id);

        if (reactionMessage == null) {
            return;
        }

        ReactionConsumer consumer = reactionMessage.find(emojiId);
        if (consumer == null) {
            consumer = reactionMessage.getNotRegisteredConsumer();
        }

        if (consumer != null) {
            consumer.handle(user, event);
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        long id = event.getMessageIdLong();

        this.reactionService.delete(id);
    }
}
