package org.imanity.framework.hologram;

import gnu.trove.iterator.TIntObjectIterator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.imanity.framework.hologram.api.ViewHandler;
import org.imanity.framework.util.ReflectionUtil;
import org.imanity.framework.util.SpigotUtil;
import org.imanity.framework.util.nms.NMSUtil;

import java.util.Collection;

@Getter
@Setter
public class HologramSingle {

    private Hologram hologram;

    private int index;
    private boolean packetsBuilt;
    private int[] entityIds;
    private float y;

    private ViewHandler viewHandler;

    public HologramSingle(Hologram hologram, ViewHandler viewHandler, float y, int index) {
        this.hologram = hologram;
        this.y = y;
        this.viewHandler = viewHandler;
        this.index = index;
    }

    public Location getLocation() {
        return hologram.getLocation().clone().add(0, this.y, 0);
    }

    public void send(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendSpawnPacket(players);
            this.sendTeleportPacket(players);
            this.sendNamePackets(players);
            this.sendAttachPacket(players);
        }

    }

    public void sendRemove(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendDestroyPacket(players);
        }

    }

    public void build(boolean rebuild) {
        if ((!rebuild) && (this.packetsBuilt))
            throw new IllegalStateException("packets already built");
        if ((rebuild) && (!this.packetsBuilt))
            throw new IllegalStateException("cannot rebuild packets before building once");

        Location location = getLocation();
        String defaultName = this.viewHandler.view(null);

        EntityHorse horse1_7 = this.buildHorse(location.clone().add(0.0D, 54.56D, 0.0D), true, defaultName);
        EntityHorse horse1_8 = this.buildHorse(location.clone().add(0.0D, -2.25D, 0.0D), false, defaultName);
        EntityWitherSkull witherSkull = this.buildWitherSkull(location.clone().add(0.0D, 54.56D, 0.0D));

        this.witherSkullDataWatcher = witherSkull.getDataWatcher();

        if (rebuild) {

            ReflectionUtil.setDeclaredField(witherSkull, "id", this.entityIds[0]);
            ReflectionUtil.setDeclaredField(horse1_7, "id", this.entityIds[1]);
            ReflectionUtil.setDeclaredField(horse1_8, "id", this.entityIds[2]);

            Entity.setEntityCount(Entity.getEntityCount() - 3);

        } else {
            this.entityIds = new int[] {
                witherSkull.getId(),
                horse1_7.getId(),
                horse1_8.getId()
            };
        }

        {
            this.packetHorse1_7 = new PacketPlayOutSpawnEntityLiving(horse1_7);
            this.horseDataWatcher1_7 = this.packetHorse1_7.getL();

            if (defaultName != null) {
                NMSUtil.setDataWatcher(this.horseDataWatcher1_7, 10, defaultName);
            }
            NMSUtil.setDataWatcher(this.horseDataWatcher1_7, 11, (byte) (defaultName != null && !defaultName.isEmpty() ? 1 : 0));
            NMSUtil.setDataWatcher(this.horseDataWatcher1_7, 12, -1700000);
        }

        {
            this.packetHorse1_8 = new PacketPlayOutSpawnEntityLiving(horse1_8);
            this.packetHorse1_8.setB(30);

            this.horseDataWatcher1_8 = this.packetHorse1_8.getL();
            this.horseDataWatcher1_8.dataValues.put(10, new DataWatcher.WatchableObject(0, 10, (byte) 1));

            IntList toRemove = new IntArrayList();

            for (Object object : this.horseDataWatcher1_8.dataValues.values()) {

                DataWatcher.WatchableObject watchableObject = (DataWatcher.WatchableObject) object;
                if (watchableObject != null) {
                    int index = watchableObject.a();
                    if (index == 2) {
                        watchableObject.a(defaultName);
                    } else if (index != 3) {
                        toRemove.add(index);
                    }
                }
            }

            toRemove.forEach(this.horseDataWatcher1_8.dataValues::remove);

            this.horseDataWatcher1_8.dataValues.put(0, new DataWatcher.WatchableObject(0, 0, (byte) 32));
        }

        this.packetWitherSkull = new PacketPlayOutSpawnEntity(witherSkull, EntityType.WITHER_SKULL.getTypeId());
        this.packetWitherSkull.setJ(66);

        this.attachPacket = new PacketPlayOutAttachEntity(0, horse1_7, witherSkull);
        this.attachPacket.setB(this.entityIds[1]);
        this.attachPacket.setC(this.entityIds[0]);

        this.packetTeleportSkull = this.buildTeleportPacket(this.entityIds[0], location.clone().add(0, 54.56D, 0), true);
        this.packetTeleportHorse1_7 = this.buildTeleportPacket(this.entityIds[0], location.clone().add(0, 54.56D, 0), true);
        this.packetTeleportHorse1_8 = this.buildTeleportPacket(this.entityIds[0], location.clone().add(0, -2.25D, 0), true);

        this.ridingAttachPacket = new PacketPlayOutAttachEntity();
        this.ridingAttachPacket.setA(0);
        this.ridingAttachPacket.setB(this.entityIds[0]);
        this.ridingAttachPacket.setC(this.hologram.getAttachedTo() != null ? this.hologram.getAttachedTo().getEntityId() : -1);

        this.ridingEjectPacket = new PacketPlayOutAttachEntity();
        this.ridingEjectPacket.setA(0);
        this.ridingEjectPacket.setB(this.entityIds[0]);
        this.ridingEjectPacket.setC(-1);

        if (!rebuild) {
            this.packetDestroy = new PacketPlayOutEntityDestroy(this.entityIds);
        }

    }

    private PacketPlayOutEntityTeleport buildTeleportPacket(int id, Location location, boolean onGround) {
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();

        packet.setA(id);
        packet.setB((int) (location.getX() * 32.0D));
        packet.setC((int) (location.getY() * 32.0D));
        packet.setD((int) (location.getZ() * 32.0D));

        packet.setE((byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        packet.setF((byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        return packet;
    }

    protected void sendSpawnPacket(Collection<? extends Player> players) {

        players.forEach(player -> {

            if (SpigotUtil.getProtocolVersion(player) > 5) {
                ReflectionUtil.sendPacket(player, this.packetHorse1_8);
            } else {
                ReflectionUtil.sendPacket(player, this.packetHorse1_7);
                ReflectionUtil.sendPacket(player, this.packetWitherSkull);
                ReflectionUtil.sendPacket(player, this.attachPacket);
            }

        });

    }

    protected void sendTeleportPacket(Collection<? extends Player> players) {
        players.forEach(player -> {

            if (SpigotUtil.getProtocolVersion(player) > 5) {
                ReflectionUtil.sendPacket(player,  packetTeleportHorse1_8);
            } else {
                ReflectionUtil.sendPacket(player, this.packetTeleportHorse1_7);
                ReflectionUtil.sendPacket(player, this.packetTeleportSkull);
            }

        });
    }

    protected void sendNamePackets(Collection<? extends Player> players) {
        players.forEach(player -> {
            int protocol = SpigotUtil.getProtocolVersion(player);

            int id = protocol > 5 ? this.entityIds[2] : this.entityIds[1];
            DataWatcher dataWatcher = protocol > 5 ? this.horseDataWatcher1_8 : this.horseDataWatcher1_7;

            String name = this.viewHandler.view(player);
            PacketPlayOutEntityMetadata packet = this.buildNamePacket(id, dataWatcher, 2, 3, name);

            ReflectionUtil.sendPacket(player, packet);

            if (protocol <= 5
                && this.entityIds.length > 1) {

                ReflectionUtil.sendPacket(player, this.buildNamePacket(this.entityIds[1], this.horseDataWatcher1_7, 10, 11, name));

            }
        });
    }

    protected void sendDestroyPacket(Collection<? extends Player> players) {
        players.forEach(player -> ReflectionUtil.sendPacket(player, this.packetDestroy));
    }

    protected void sendAttachPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (this.hologram.isAttached()) {
                ReflectionUtil.sendPacket(player, this.ridingEjectPacket);
            } else {
                ReflectionUtil.sendPacket(player, this.ridingAttachPacket);
            }
        });
    }

    private PacketPlayOutEntityDestroy packetDestroy;
    private PacketPlayOutEntityTeleport packetTeleportSkull, packetTeleportHorse1_7, packetTeleportHorse1_8;
    private PacketPlayOutSpawnEntity packetWitherSkull;
    private PacketPlayOutSpawnEntityLiving packetHorse1_7, packetHorse1_8;
    private PacketPlayOutAttachEntity attachPacket, ridingAttachPacket, ridingEjectPacket;
    private DataWatcher witherSkullDataWatcher, horseDataWatcher1_7, horseDataWatcher1_8;

    private PacketPlayOutEntityMetadata buildNamePacket(int id, DataWatcher dataWatcher, int nameIndex, int visibilityIndex, String name) {
        NMSUtil.setDataWatcher(dataWatcher, nameIndex, name != null ? name : "");
        NMSUtil.setDataWatcher(dataWatcher, visibilityIndex, (byte) (name != null && !name.isEmpty() ? 1 : 0));

        return new PacketPlayOutEntityMetadata(id, dataWatcher, true);
    }

    private EntityWitherSkull buildWitherSkull(Location location) {
        EntityWitherSkull entityWitherSkull = new EntityWitherSkull(((CraftWorld) location.getWorld()).getHandle());
        entityWitherSkull.locX = location.getX();
        entityWitherSkull.locY = location.getY();
        entityWitherSkull.locZ = location.getZ();
        return entityWitherSkull;
    }

    private EntityHorse buildHorse(Location location, boolean datawatcher, String defaultName) {
        EntityHorse entityHorse = new EntityHorse(((CraftWorld) location.getWorld()).getHandle());
        entityHorse.locX = location.getX();
        entityHorse.locY = location.getY();
        entityHorse.locZ = location.getZ();

        entityHorse.setCustomName(defaultName);
        entityHorse.setCustomNameVisible(!datawatcher || (defaultName != null && !defaultName.isEmpty()));

        if (datawatcher) {
            entityHorse.setAge(-1700000);
            NMSUtil.setDataWatcher(entityHorse.getDataWatcher(), 12, (byte) 96);
        }
        return entityHorse;
    }

}
