package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@NoArgsConstructor
@Data
public class EmbedSiteProviderDto {

    private String name;
    private String url;

    public EmbedSiteProviderDto(MessageEmbed.Provider provider) {
        this.name = provider.getName();
        this.url = provider.getUrl();
    }

}
