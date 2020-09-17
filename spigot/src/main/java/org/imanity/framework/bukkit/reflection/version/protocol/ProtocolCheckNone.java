package org.imanity.framework.bukkit.reflection.version.protocol;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;

public class ProtocolCheckNone implements ProtocolCheck {
    @Override
    public int getVersion(Player player) {
        return MinecraftReflection.VERSION.version();
    }
}
