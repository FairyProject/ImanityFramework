package org.imanity.framework.bukkit.packet.wrapper.server.entity;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;

@AutowiredWrappedPacket(value = PacketType.Server.REL_ENTITY_MOVE, direction = PacketDirection.WRITE)
@Getter
public class WrappedPacketOutRelEntityMove extends WrappedPacket {

    private int entityID;
    private Entity entity;
    private double deltaX, deltaY, deltaZ;
    private boolean onGround;

    public WrappedPacketOutRelEntityMove(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {
        int dX = 1, dY = 1, dZ = 1;
        switch (EntityPacketUtil.getMode()) {
            case 0:
                dX = readByte(0);
                dY = readByte(1);
                dZ = readByte(2);
                break;
            case 1:
                dX = readInt(1);
                dY = readInt(2);
                dZ = readInt(3);
                break;
            case 2:
                dX = readShort(0);
                dY = readShort(1);
                dZ = readShort(2);
                break;
        }
        deltaX = dX / EntityPacketUtil.getDXYZDivisor();
        deltaY = dY / EntityPacketUtil.getDXYZDivisor();
        deltaZ = dZ / EntityPacketUtil.getDXYZDivisor();
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