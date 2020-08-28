package org.imanity.framework.bukkit.tablist.utils.player;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.tablist.ImanityTabHandler;
import org.imanity.framework.bukkit.tablist.utils.version.PlayerVersion;

public class PlayerUtil {

    public static PlayerVersion getPlayerVersion(Player player) {
        return PlayerVersion.getVersionFromRaw(ImanityTabHandler.getInstance().getProtocolCheck().getVersion(player));
    }

}
