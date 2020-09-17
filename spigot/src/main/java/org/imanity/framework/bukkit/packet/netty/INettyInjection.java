package org.imanity.framework.bukkit.packet.netty;

import org.bukkit.entity.Player;

public interface INettyInjection {

    void inject(Player player);

    void eject(Player player);

    void registerChannels() throws Exception;

    void unregisterChannels();

    byte[] readBytes(Object byteBuffer);

}
