package org.imanity.framework.bukkit.packet.netty;

import org.bukkit.entity.Player;

public interface INettyInjection {

    void inject(Player player);

    void eject(Player player);

    byte[] readBytes(Object byteBuffer);

}
