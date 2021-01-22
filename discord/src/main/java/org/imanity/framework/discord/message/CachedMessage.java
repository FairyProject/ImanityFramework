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
