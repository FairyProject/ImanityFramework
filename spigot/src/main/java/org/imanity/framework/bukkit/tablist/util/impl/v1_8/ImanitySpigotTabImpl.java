package org.imanity.framework.bukkit.tablist.util.impl.v1_8;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutLogin;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import spg.lgdev.handler.PacketHandler;
import spg.lgdev.iSpigot;

public class ImanitySpigotTabImpl extends NMS1_8TabImpl {

    @Override
    public void registerLoginListener() {
        iSpigot.INSTANCE.addPacketHandler(new PacketHandler() {
            @Override
            public void handleReceivedPacket(PlayerConnection playerConnection, Packet packet) {

            }

            @Override
            public void handleSentPacket(PlayerConnection playerConnection, Packet packet) {

                if (packet instanceof PacketPlayOutLogin) {

                    PacketPlayOutLogin loginPacket = (PacketPlayOutLogin) packet;
                    loginPacket.setF(60);

                }

            }
        });
    }
}
