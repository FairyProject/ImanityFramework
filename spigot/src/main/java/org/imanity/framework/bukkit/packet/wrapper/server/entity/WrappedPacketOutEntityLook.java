package org.imanity.framework.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.ENTITY_LOOK, direction = PacketDirection.WRITE)
@Getter
public class WrappedPacketOutEntityLook extends WrappedPacket {
    private int entityID;
    private Entity entity;
    private byte yaw, pitch;
    private boolean onGround;

    public WrappedPacketOutEntityLook(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        entityID = readInt(0);
        onGround = readBoolean(0);
        switch (EntityPacketUtil.getMode()) {
            case 0:
                yaw = readByte(3);
                pitch = readByte(4);
                break;
            case 1:
            case 2:
                yaw = readByte(0);
                pitch = readByte(1);
                break;
        }
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     *
     * @return Entity
     */
    public Entity getEntity() {
        if (entity != null) {
            return entity;
        }
        return entity = Imanity.IMPLEMENTATION.getEntity(this.entityID);
    }

}