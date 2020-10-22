package org.imanity.framework.discord.command;

import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.imanity.framework.command.CommandEvent;
import org.imanity.framework.command.CommandMeta;
import org.imanity.framework.discord.DiscordService;
import org.imanity.framework.discord.annotations.OnlyGuild;
import org.imanity.framework.discord.annotations.OnlyPrivate;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class DiscordCommandEvent extends CommandEvent {

    @Getter
    private final MessageChannel channel;

    public DiscordCommandEvent(Member user, String command, MessageChannel channel) {
        super(user, command);

        this.channel = channel;
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
    public void sendUsage(String usage) {
        DiscordService.INSTANCE.getPresenceProvider().sendUsage(this, usage);
    }

    @Override
    public void sendError(Throwable exception) {
        DiscordService.INSTANCE.getPresenceProvider().sendError(this, exception);
    }

    @Override
    public void sendNoPermission() {
        DiscordService.INSTANCE.getPresenceProvider().sendNoPermission(this);
    }

    @Override
    public void sendInternalError(String message) {
        DiscordService.INSTANCE.getPresenceProvider().sendInternalError(this, message);
    }

    @Override
    public boolean shouldExecute(CommandMeta meta, String[] arguments) {

        if (meta.getMethod().getAnnotation(OnlyPrivate.class) != null && !this.isPrivate()) {
            return false;
        }

        if (meta.getMethod().getAnnotation(OnlyGuild.class) != null && !this.isGuild()) {
            return false;
        }

        return true;
    }
}
