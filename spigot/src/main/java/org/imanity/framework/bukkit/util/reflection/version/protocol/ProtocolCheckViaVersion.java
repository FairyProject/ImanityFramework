package org.imanity.framework.bukkit.util.reflection.version.protocol;

import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;

public class ProtocolCheckViaVersion implements ProtocolCheck {
    @Override
    public int getVersion(Player player) {
        return Via.getAPI().getPlayerVersion(player.getUniqueId());
    }
}
