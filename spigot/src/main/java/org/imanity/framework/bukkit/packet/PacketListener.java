package org.imanity.framework.bukkit.packet;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;

public interface PacketListener {

    byte[] type();

    boolean read(Player player, WrappedPacket packet);

    boolean write(Player player, WrappedPacket packet);

}
