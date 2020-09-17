package org.imanity.framework.bukkit.packet.wrapper.client.login;

import lombok.Getter;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.wrapper.GameProfileWrapper;

@AutowiredWrappedPacket(value = PacketType.Client.LOGIN_START, direction = PacketDirection.READ)
@Getter
public class WrappedPacketInLoginStart extends WrappedPacket {

    private GameProfileWrapper gameProfile;

    public WrappedPacketInLoginStart(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        this.gameProfile = this.readGameProfile(0);
    }

}
