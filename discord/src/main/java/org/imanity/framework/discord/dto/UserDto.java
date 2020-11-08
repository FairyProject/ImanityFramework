package org.imanity.framework.discord.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.User;

@NoArgsConstructor
@Data
public class UserDto {

    private long id;
    private String username;
    private String discriminator;
    private String avatarURL;

    public UserDto(User user) {
        this.id = user.getIdLong();
        this.username = user.getName();
        this.discriminator = user.getDiscriminator();
        this.avatarURL = user.getAvatarUrl();
    }

}
