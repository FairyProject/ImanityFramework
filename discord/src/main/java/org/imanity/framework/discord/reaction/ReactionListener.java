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
