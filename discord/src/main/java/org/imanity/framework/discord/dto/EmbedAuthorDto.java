package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@NoArgsConstructor
@Data
public class EmbedAuthorDto {

    private String name;
    private String url;
    private String icon_url;

    public EmbedAuthorDto(MessageEmbed.AuthorInfo authorInfo) {
        this.name = authorInfo.getName();
        this.url = authorInfo.getUrl();
        this.icon_url = authorInfo.getIconUrl();
    }

}
