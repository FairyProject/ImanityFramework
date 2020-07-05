package org.imanity.framework.player.data.example;

import org.bukkit.entity.Player;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.annotation.StoreData;

public class ExamplePlayerData extends PlayerData {

    @StoreData
    public String language = "zh_tw";

    public ExamplePlayerData(Player player) {
        super(player);
    }

    public static void register() {
        PlayerData.builder()
                .name("example")
                .playerDataClass(ExamplePlayerData.class)
                .loadOnJoin(true)
                .saveOnQuit(true)
                .build();
    }
}
