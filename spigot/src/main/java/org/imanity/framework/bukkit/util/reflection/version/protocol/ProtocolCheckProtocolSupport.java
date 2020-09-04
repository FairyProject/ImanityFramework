package org.imanity.framework.bukkit.util.reflection.version.protocol;

import org.bukkit.entity.Player;
import protocolsupport.api.ProtocolSupportAPI;

public class ProtocolCheckProtocolSupport implements ProtocolCheck {
    @Override
    public int getVersion(Player player) {
        return ProtocolSupportAPI.getProtocolVersion(player).getId();
    }
}
