package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class EmbedDto {

    private String url;
    private String title;
    private String description;
    private Long timestamp;
    private int color;
    private String thumbnail;
    private EmbedSiteProviderDto provider;
    private EmbedAuthorDto author;
    private String video;
    private EmbedFooterDto footer;
    private String image;

    private List<EmbedFieldDto> fields;

    public EmbedDto(MessageEmbed embed) {
        this.url = embed.getUrl();
        this.title = embed.getTitle();
        this.description = embed.getDescription();
        this.timestamp = embed.getTimestamp() != null ? embed.getTimestamp().toInstant().toEpochMilli() : null;
        this.color = embed.getColorRaw();
        this.thumbnail = embed.getThumbnail() != null ? embed.getThumbnail().getUrl() : null;
        this.provider = embed.getSiteProvider() != null ? new EmbedSiteProviderDto(embed.getSiteProvider()) : null;
        this.author = embed.getAuthor() != null ? new EmbedAuthorDto(embed.getAuthor()) : null;
        this.video = embed.getVideoInfo() != null ? embed.getVideoInfo().getUrl() : null;
        this.footer = embed.getFooter() != null ? new EmbedFooterDto(embed.getFooter()) : null;
        this.image = embed.getImage() != null ? embed.getImage().getUrl() : null;
        this.fields = embed.getFields().stream()
                .map(EmbedFieldDto::new)
                .collect(Collectors.toList());
    }

}
