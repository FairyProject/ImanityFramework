package org.imanity.framework.bukkit.packet.wrapper.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.SendableWrapper;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.minecraft.DataWatcher;
import org.imanity.framework.bukkit.reflection.wrapper.DataWatcherWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.PacketWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.WatchableObjectWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AutowiredWrappedPacket(value = PacketType.Server.SPAWN_ENTITY_LIVING, direction = PacketDirection.WRITE)
@Getter
@Setter
@AllArgsConstructor
@Builder
public class WrappedPacketOutSpawnEntityLiving extends WrappedPacket implements SendableWrapper {
    public WrappedPacketOutSpawnEntityLiving(Object packet) {
        super(packet);
    }

    private int entityId, entityTypeId;
    private int locX, locY, locZ;

    private float yaw, pitch, headPitch;

    private double velX, velY, velZ;

    private DataWatcherWrapper dataWatcher;
    private List<WatchableObjectWrapper> watchableObjects;

    @Override
    protected void setup() {
        this.entityId = readInt(0);
        this.entityTypeId = readInt(1);

        this.locX = readInt(2) / 32;
        this.locY = readInt(3) / 32;
        this.locZ = readInt(4) / 32;

        this.yaw = readByte(0) / 256.0F * 360.0F;
        this.pitch = readByte(1) / 256.0F * 360.0F;
        this.headPitch = readByte(2) / 256.0F * 360.0F;

        this.velX = readInt(5) / 8000.0D;
        this.velY = readInt(6) / 8000.0D;
        this.velZ = readInt(7) / 8000.0D;

        this.dataWatcher = new DataWatcherWrapper(readObject(0, DataWatcher.TYPE));
        this.watchableObjects = new ArrayList<>();
        for (Object watchableObject : readObject(0, List.class)) {
            this.watchableObjects.add(WatchableObjectWrapper.getConverter().getSpecific(watchableObject));
        }
    }

    @Override
    public Object asNMSPacket() {
        return new PacketWrapper(PacketTypeClasses.Server.TITLE)
                .setFieldByIndex(int.class, 0, this.entityId)
                .setFieldByIndex(int.class, 1, this.entityTypeId)
                .setFieldByIndex(int.class, 2, Math.floor(this.locX * 32.0D))
                .setFieldByIndex(int.class, 3, Math.floor(this.locY * 32.0D))
                .setFieldByIndex(int.class, 4, Math.floor(this.locZ * 32.0D))
                .setFieldByIndex(byte.class, 0, (byte) ((int)(this.yaw * 256.0F / 360.0F)))
                .setFieldByIndex(byte.class, 1, (byte) ((int)(this.pitch * 256.0F / 360.0F)))
                .setFieldByIndex(byte.class, 2, (byte) ((int)(this.headPitch * 256.0F / 360.0F)))
                .setFieldByIndex(int.class, 5, (int) this.velX * 8000.0D)
                .setFieldByIndex(int.class, 6, (int) this.velY * 8000.0D)
                .setFieldByIndex(int.class, 7, (int) this.velZ * 8000.0D)
                .setFieldByIndex(DataWatcher.TYPE, 0, this.dataWatcher.getDataWatcherObject())
                .setFieldByIndex(List.class, 0, this.watchableObjects.stream().map(watchableObjectWrapper -> WatchableObjectWrapper.getConverter().getGeneric(watchableObjectWrapper)).collect(Collectors.toList()))
                .getPacket();
    }

}
