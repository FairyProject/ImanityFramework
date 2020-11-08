package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;

@NoArgsConstructor
@Data
public class EmbedFieldDto {

    private String name;
    private String value;
    private boolean inline;

    public EmbedFieldDto(MessageEmbed.Field field) {
        this.name = field.getName();
        this.value = field.getValue();
        this.inline = field.isInline();
    }

}
