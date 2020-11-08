package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.MessageChannel;

@NoArgsConstructor
@Data
public class ChannelDto {

    private long id;
    private String name;
    private long createdTimestamp;

    public ChannelDto(MessageChannel channel) {
        this.id = channel.getIdLong();
        this.name = channel.getName();
        this.createdTimestamp = channel.getTimeCreated().toInstant().toEpochMilli();
    }

}
