package org.imanity.framework.bukkit.util.reflection.version.protocol;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.entity.Player;

public class ProtocolCheckProtocolLib implements ProtocolCheck {
    @Override
    public int getVersion(Player player) {
        return ProtocolLibrary.getProtocolManager().getProtocolVersion(player);
    }
}
