package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class MessageDto {

    private long channelID;
    private boolean deleted;
    private long id;
    private long authorID;

    private List<EmbedDto> embeds;

    private List<AttachmentDto> attachments;
    private long createdTimestamp;
    private String cleanContent;

    public MessageDto(Message message) {
        this.channelID = message.getChannel().getIdLong();

        this.deleted = false;
        this.id = message.getIdLong();
        this.authorID = message.getAuthor().getIdLong();

        this.embeds = message.getEmbeds().stream()
                .map(EmbedDto::new)
                .collect(Collectors.toList());

        this.attachments = message.getAttachments().stream()
            .map(AttachmentDto::new)
            .collect(Collectors.toList());
        this.createdTimestamp = message.getTimeCreated().toInstant().toEpochMilli();
        this.cleanContent = message.getContentRaw();
    }

}
