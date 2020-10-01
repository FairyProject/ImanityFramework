package org.imanity.framework.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.imanity.framework.player.IPlayerBridge;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class PlayerData extends AbstractData {

    public static IPlayerBridge PLAYER_BRIDGE;

    public PlayerData(Object player) {
        this(PlayerData.PLAYER_BRIDGE.getUUID(player),
                PlayerData.PLAYER_BRIDGE.getName(player));
    }

    @JsonProperty
    private String name;

    public PlayerData(UUID uuid, String name) {
        super(uuid);
        this.name = name;
    }

    public PlayerData(UUID uuid) {
        this(uuid, "");
    }

    public static PlayerDataBuilder builder() {
        return new PlayerDataBuilder();
    }

}
