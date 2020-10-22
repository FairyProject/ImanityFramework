package org.imanity.framework.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.imanity.framework.command.InternalCommandEvent;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class DiscordCommandEvent extends InternalCommandEvent {

    @Getter
    private final MessageChannel channel;

    public DiscordCommandEvent(Member user, String command, MessageChannel channel) {
        super(user, command);

        this.channel = channel;
    }

    public boolean isPrivate() {
        return this.channel instanceof PrivateChannel;
    }

    public boolean isGuild() {
        return this.channel instanceof GuildChannel;
    }

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

    public CompletableFuture<Message> reply(File file, AttachmentOption... options) {
        return this.channel.sendFile(file, options).submit();
    }

    public CompletableFuture<Message> reply(File file, String name, AttachmentOption... options) {
        return this.channel.sendFile(file, name, options).submit();
    }

    public CompletableFuture<Void> typing() {
        return this.channel.sendTyping().submit();
    }
}
