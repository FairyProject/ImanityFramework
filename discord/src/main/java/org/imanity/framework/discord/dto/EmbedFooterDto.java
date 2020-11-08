package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@NoArgsConstructor
@Data
public class EmbedFooterDto {

    private String text;
    private String icon_url;

    public EmbedFooterDto(MessageEmbed.Footer footer) {
        this.text = footer.getText();
        this.icon_url = footer.getIconUrl();
    }

}
