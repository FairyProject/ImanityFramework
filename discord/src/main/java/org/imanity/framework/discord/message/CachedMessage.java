package org.imanity.framework.discord.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.imanity.framework.discord.DiscordService;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Id;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CachedMessage {

    @Id
    @JsonProperty("_id")
    private long messageId;
    private long channelId;

    public CachedMessage(Message message) {
        this.channelId = message.getChannel().getIdLong();
        this.messageId = message.getIdLong();
    }

    public boolean equals(Message message) {
        return this.channelId == message.getChannel().getIdLong()
                && this.messageId == message.getIdLong();
    }

    @Nullable
    public RestAction<Message> getMessage() {
        try {
            TextChannel textChannel = this.getChannel();

            if (textChannel == null) {
                return null;
            }

            return textChannel.retrieveMessageById(messageId);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Nullable
    public TextChannel getChannel() {
        return DiscordService.INSTANCE
                .getJda()
                .getTextChannelById(channelId);
    }

    public CompletableFuture<Void> delete() {
        TextChannel textChannel = DiscordService.INSTANCE
                .getJda()
                .getTextChannelById(channelId);

        if (textChannel == null) {
            return CompletableFuture.completedFuture(null);
        }

        return textChannel.deleteMessageById(messageId).submit();
    }

}
