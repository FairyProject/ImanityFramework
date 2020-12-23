/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.CommandMeta;
import org.imanity.framework.discord.DiscordService;
import org.imanity.framework.discord.annotations.OnlyChannel;
import org.imanity.framework.discord.annotations.OnlyGuild;
import org.imanity.framework.discord.annotations.OnlyPrivate;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
public class DiscordCommandEvent extends CommandEvent {

    private final MessageChannel channel;
    private final Message message;

    public DiscordCommandEvent(Member user, String command, MessageChannel channel, Message message) {
        super(user, command);

        this.channel = channel;
        this.message = message;
    }

    @Override
    public String name() {
        return "discord-user";
    }

    public boolean isPrivate() {
        return this.channel instanceof PrivateChannel;
    }

    public boolean isGuild() {
        return this.channel instanceof GuildChannel;
    }

    @Nullable
    public Member getMember() {
        return (Member) this.getUser();
    }

    public CompletableFuture<Message> reply(MessageEmbed embed) {
        return this.channel.sendMessage(embed).submit();
    }

    public CompletableFuture<Message> reply(String message) {
        return this.channel.sendMessage(message).submit();
    }

    public CompletableFuture<Message> reply(Message message) {
        return this.channel.sendMessage(message).submit();
    }

    public CompletableFuture<Void> replyTemporary(MessageEmbed embed) {
        return this.reply(embed).thenAccept(message -> message.delete().queueAfter(5L, TimeUnit.SECONDS));
    }

    public CompletableFuture<Void> replyTemporary(String msg) {
        return this.reply(msg).thenAccept(message -> message.delete().queueAfter(5L, TimeUnit.SECONDS));
    }

    public CompletableFuture<Void> replyTemporary(Message msg) {
        return this.reply(msg).thenAccept(message -> message.delete().queueAfter(5L, TimeUnit.SECONDS));
    }

    public CompletableFuture<Message> reply(File file, AttachmentOption... options) {
        return this.channel.sendFile(file, options).submit();
    }

    public CompletableFuture<Message> reply(File file, String name, AttachmentOption... options) {
        return this.channel.sendFile(file, name, options).submit();
    }

    public CompletableFuture<Void> typing() {
        return this.channel.sendTyping().submit();
    }

    @Override
    public boolean shouldExecute(CommandMeta meta, String[] arguments) {

        if (meta.getMethod().getAnnotation(OnlyPrivate.class) != null && !this.isPrivate()) {
            return false;
        }

        if (meta.getMethod().getAnnotation(OnlyGuild.class) != null && !this.isGuild()) {
            return false;
        }

        OnlyChannel onlyChannel = meta.getMethod().getAnnotation(OnlyChannel.class);
        if (onlyChannel != null && this.channel.getIdLong() != onlyChannel.value()) {
            return false;
        }

        return true;
    }
}
