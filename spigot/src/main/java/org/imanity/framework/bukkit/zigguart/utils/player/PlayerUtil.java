package org.imanity.framework.bukkit.zigguart.utils.player;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.zigguart.ImanityTabHandler;
import org.imanity.framework.bukkit.zigguart.utils.version.PlayerVersion;

public class PlayerUtil {

    public static PlayerVersion getPlayerVersion(Player player) {
        return PlayerVersion.getVersionFromRaw(ImanityTabHandler.getInstance().getProtocolCheck().getVersion(player));
    }

}
