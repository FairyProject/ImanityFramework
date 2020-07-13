package org.imanity.framework.locale.player;

import org.bukkit.entity.Player;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.annotation.StoreData;

import java.util.Locale;

public class LocaleData extends PlayerData {

    @StoreData
    private Locale locale;

    public LocaleData(Player player) {
        super(player);
    }
}
