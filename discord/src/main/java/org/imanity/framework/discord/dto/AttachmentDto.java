package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

@NoArgsConstructor
@Data
public class AttachmentDto {

    private long id;
    private String url;
    private String proxyUrl;
    private String fileName;
    private int size;
    private int height;
    private int width;

    public AttachmentDto(Message.Attachment attachment) {
        this.id = attachment.getIdLong();
        this.url = attachment.getUrl();
        this.proxyUrl = attachment.getProxyUrl();
        this.fileName = attachment.getFileName();
        this.size = attachment.getSize();
        this.height = attachment.getHeight();
        this.width = attachment.getWidth();
    }

}
