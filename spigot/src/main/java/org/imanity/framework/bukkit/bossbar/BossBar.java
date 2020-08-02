package org.imanity.framework.bukkit.bossbar;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.ReflectionUtil;
import org.imanity.framework.bukkit.util.nms.NMSUtil;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class BossBar {

    private static final int ENTITY_DISTANCE = 32;
    private static final int MAX_HEALTH = 300;

    private final int entityId;

    private Player player;

    private boolean visible;
    private String previousText;
    private float previousHealth;
    private DataWatcher dataWatcher;

    @Getter
    private AtomicBoolean moved = new AtomicBoolean(false);

    public BossBar(Player player) {
        this.player = player;

        this.entityId = Entity.getEntityCount() + 1;
        Entity.setEntityCount(this.entityId);

        this.buildPackets();
    }

    public Location makeLocation(Location base) {
        return base.getDirection().multiply(ENTITY_DISTANCE).add(base.toVector()).toLocation(base.getWorld());
    }

    private PacketPlayOutSpawnEntityLiving packetWither;

    private void buildPackets() {
        this.packetWither = new PacketPlayOutSpawnEntityLiving();
        this.packetWither.setA(this.entityId);
        this.packetWither.setB(64);

        this.packetWither.setC(0);
        this.packetWither.setD(0);
        this.packetWither.setE(0);

        this.packetWither.setI((byte) 0);
        this.packetWither.setJ((byte) 0);
        this.packetWither.setK((byte) 0);
        this.packetWither.setL(dataWatcher);
    }

    private void buildDataWatcher() {
        this.dataWatcher = new DataWatcher((Entity) null);

        NMSUtil.setDataWatcher(dataWatcher, 17, 0);
        NMSUtil.setDataWatcher(dataWatcher, 18, 0);
        NMSUtil.setDataWatcher(dataWatcher, 19, 0);

        NMSUtil.setDataWatcher(dataWatcher, 20, 1000);
        NMSUtil.setDataWatcher(dataWatcher, 0, (byte) (0 | 1 << 5));
    }

    private void updateDataWatcher(BossBarData bossBarData) {
        if (this.dataWatcher == null) {
            this.buildDataWatcher();
        }

        NMSUtil.setDataWatcher(this.dataWatcher, 6, bossBarData.getHealth() / 100.0F * MAX_HEALTH);
        NMSUtil.setDataWatcher(this.dataWatcher, 10, bossBarData.getText());
        NMSUtil.setDataWatcher(this.dataWatcher, 2, bossBarData.getText());
        NMSUtil.setDataWatcher(this.dataWatcher, 11, (byte) 1);
        NMSUtil.setDataWatcher(this.dataWatcher, 3, (byte) 1);
    }

    private void sendMetadata(BossBarData bossBarData) {
        DataWatcher dataWatcher = this.dataWatcher.clone();

        if (bossBarData.getText().equals(this.previousText)
            && bossBarData.getHealth() == this.previousHealth) {
            return;
        }

        this.previousText = bossBarData.getText();
        this.previousHealth = bossBarData.getHealth();

        PacketPlayOutEntityMetadata packetMetadata = new PacketPlayOutEntityMetadata(this.entityId, dataWatcher, true);
        ReflectionUtil.sendPacket(player, packetMetadata);
    }

    private void sendMovement() {
        Location location = this.makeLocation(player.getLocation());

        PacketPlayOutEntityTeleport packetTeleport = new PacketPlayOutEntityTeleport();
        packetTeleport.setA(this.entityId);

        packetTeleport.setB((int) (location.getX() * 32D));
        packetTeleport.setC((int) (location.getY() * 32D));
        packetTeleport.setD((int) (location.getZ() * 32D));

        packetTeleport.setE((byte) (int) (location.getYaw() * 256F / 360F));
        packetTeleport.setF((byte) (int) (location.getPitch() * 256F / 360F));

        ReflectionUtil.sendPacket(player, packetTeleport);
    }

    public void send(BossBarData bossBarData) {

        boolean movement = this.moved.get();

        if (!this.visible) {
            this.visible = true;
            this.updateDataWatcher(bossBarData);
            this.buildPackets();

            ReflectionUtil.sendPacket(player, this.packetWither);

            movement = true;
        } else {
            this.updateDataWatcher(bossBarData);
        }

        this.sendMetadata(bossBarData);
        if (movement) {
            this.sendMovement();
        }

    }

    public void destroy(Player player) {
        if (!visible) {
            return;
        }

        PacketPlayOutEntityDestroy packetDestroy = new PacketPlayOutEntityDestroy(this.entityId);
        ReflectionUtil.sendPacket(player, packetDestroy);
        this.visible = false;
    }

}
