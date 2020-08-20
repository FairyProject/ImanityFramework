package org.imanity.framework.data;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.data.store.StoreType;
import org.imanity.framework.player.IPlayerBridge;
import org.imanity.framework.player.PlayerInfo;
import org.imanity.framework.data.annotation.StoreData;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.data.type.DataConverter;
import org.imanity.framework.data.type.DataConverterType;
import org.imanity.framework.util.entry.EntryArrayList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Getter
@Setter
public class PlayerData extends AbstractData {

    public static IPlayerBridge PLAYER_BRIDGE;

    public PlayerData(Object player) {
        this(PlayerData.PLAYER_BRIDGE.getUUID(player),
                PlayerData.PLAYER_BRIDGE.getName(player));
    }

    @StoreData
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
