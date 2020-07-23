package org.imanity.framework.locale.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.player.data.PlayerData;
import org.imanity.framework.player.data.annotation.StoreData;

@Getter
@Setter
public class LocaleData extends PlayerData {

    @StoreData
    private Locale locale;

    public LocaleData(Player player) {
        super(player);
    }
}
