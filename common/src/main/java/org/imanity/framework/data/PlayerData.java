package org.imanity.framework.data;

import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.player.IPlayerBridge;
import org.imanity.framework.data.annotation.StoreDataElement;

import java.util.*;

@Getter
@Setter
public class PlayerData extends AbstractData {

    public static IPlayerBridge PLAYER_BRIDGE;

    public PlayerData(Object player) {
        this(PlayerData.PLAYER_BRIDGE.getUUID(player),
                PlayerData.PLAYER_BRIDGE.getName(player));
    }

    @StoreDataElement
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
