package org.imanity.framework.bukkit.hologram;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.api.ViewHandler;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.MethodResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.*;
import org.imanity.framework.bukkit.util.reflection.minecraft.DataWatcher;
import org.imanity.framework.util.CommonUtility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class HologramSingle {

    private static final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

    private static Class<?> ENTITY_HORSE_TYPE;
    private static Class<?> ENTITY_WITHER_SKULL_TYPE;

    private static FieldResolver PACKET_SPAWN_LIVING_FIELD_RESOLVER;

    private static ConstructorWrapper<?> ENTITY_HORSE_CONSTRUCTOR;
    private static ConstructorWrapper<?> ENTITY_WITHER_SKULL_CONSTRUCTOR;
    private static ConstructorWrapper<?> PACKET_METADATA_CONSTRUCTOR;
    private static ConstructorWrapper<?> PACKET_SPAWN_LIVING_CONSTRUCTOR;
    private static ConstructorWrapper<?> PACKET_SPAWN_CONSTRUCTOR;
    private static ConstructorWrapper<?> PACKET_ATTACH_CONTRUCTOR;
    private static ConstructorWrapper<?> PACKET_DESTORY_CONSTRUCTOR;

    private static Field ENTITY_ID_FIELD;

    static {
        try {
            Class<?> entityType = CLASS_RESOLVER.resolve("Entity");
            ENTITY_ID_FIELD = entityType.getDeclaredField("id");
            ENTITY_ID_FIELD.setAccessible(true);

            ENTITY_HORSE_TYPE = CLASS_RESOLVER.resolve("EntityHorse");
            ENTITY_WITHER_SKULL_TYPE = CLASS_RESOLVER.resolve("EntityWitherSkull");

            Class<?> worldType = CLASS_RESOLVER.resolve("World");

            ENTITY_HORSE_CONSTRUCTOR = new ConstructorWrapper<>(ENTITY_HORSE_TYPE.getConstructor(worldType));
            ENTITY_WITHER_SKULL_CONSTRUCTOR = new ConstructorWrapper<>(ENTITY_WITHER_SKULL_TYPE.getConstructor(worldType));

            Class<?> packetMetadataType = CLASS_RESOLVER.resolve("PacketPlayOutEntityMetadata");
            Class<?> dataWatcherType = DataWatcher.TYPE;
            PACKET_METADATA_CONSTRUCTOR = new ConstructorWrapper<>(packetMetadataType.getConstructor(int.class, dataWatcherType, boolean.class));

            Class<?> packetSpawnLivingType = CLASS_RESOLVER.resolve("PacketPlayOutSpawnEntityLiving");
            Class<?> entityLivingType = CLASS_RESOLVER.resolve("EntityLiving");
            PACKET_SPAWN_LIVING_CONSTRUCTOR = new ConstructorWrapper<>(packetSpawnLivingType.getConstructor(entityLivingType));
            PACKET_SPAWN_LIVING_FIELD_RESOLVER = new FieldResolver(packetSpawnLivingType);

            Class<?> packetSpawnType = CLASS_RESOLVER.resolve("PacketPlayOutSpawnEntity");
            PACKET_SPAWN_CONSTRUCTOR = new ConstructorWrapper<>(packetSpawnType.getConstructor(entityType, int.class));

            Class<?> packetAttachType = CLASS_RESOLVER.resolve("PacketPlayOutAttachEntity");
            PACKET_ATTACH_CONTRUCTOR = new ConstructorWrapper<>(packetAttachType.getConstructor(int.class, entityType, entityType));

            Class<?> packetDestory = CLASS_RESOLVER.resolve("PacketPlayOutEntityDestroy");
            PACKET_DESTORY_CONSTRUCTOR = new ConstructorWrapper<>(packetDestory.getConstructor(int[].class));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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

        try {
            Location location = getLocation();
            String defaultName = this.viewHandler.view(null);

            ObjectWrapper horse1_7 = this.buildHorse(location.clone().add(0.0D, 54.56D, 0.0D), true, defaultName);
            ObjectWrapper horse1_8 = this.buildHorse(location.clone().add(0.0D, -2.25D, 0.0D), false, defaultName);
            ObjectWrapper witherSkull = this.buildWitherSkull(location.clone().add(0.0D, 54.56D, 0.0D));

            System.out.println(horse1_8.getField("locX") + " " + horse1_8.getField("locY") + " " + horse1_8.getField("locZ"));

            this.witherSkullDataWatcher = new DataWatcherWrapper(witherSkull.invoke("getDataWatcher"));

            if (rebuild) {

                try {
                    ENTITY_ID_FIELD.set(witherSkull.getObject(), this.entityIds[0]);
                    ENTITY_ID_FIELD.set(horse1_7.getObject(), this.entityIds[1]);
                    ENTITY_ID_FIELD.set(horse1_8.getObject(), this.entityIds[2]);
                } catch (Exception ex) {
                    throw new RuntimeException("Something wrong while building packets for hologram", ex);
                }

                MinecraftReflection.setEntityId(-3);

            } else {
                this.entityIds = new int[] {
                        (int) witherSkull.invoke("getId"),
                        (int) horse1_7.invoke("getId"),
                        (int) horse1_8.invoke("getId")
                };
            }

            {
                this.packetHorse1_7 = new PacketWrapper(PACKET_SPAWN_LIVING_CONSTRUCTOR.newInstance(horse1_7.getObject()));
                this.horseDataWatcher1_7 = new DataWatcherWrapper(PACKET_SPAWN_LIVING_FIELD_RESOLVER.resolveByFirstTypeWrapper(DataWatcher.TYPE).get(this.packetHorse1_7.getPacket()));

                if (defaultName != null) {
                    this.horseDataWatcher1_7.setValue(10, DataWatcher.V1_9.ValueType.ENTITY_NAME, defaultName);
                }
                this.horseDataWatcher1_7.setValue(11, DataWatcher.V1_9.ValueType.ENTITY_NAME_VISIBLE, (byte) (defaultName != null && !defaultName.isEmpty() ? 1 : 0));
                this.horseDataWatcher1_7.setValue(12, DataWatcher.V1_9.ValueType.ENTITY_FLAG, (byte) -1700000);
            }

            {
                this.packetHorse1_8 = new PacketWrapper(PACKET_SPAWN_LIVING_CONSTRUCTOR.newInstance(horse1_8.getObject()));
                this.packetHorse1_8.setPacketValue("b", 30);

                this.horseDataWatcher1_8 = new DataWatcherWrapper(PACKET_SPAWN_LIVING_FIELD_RESOLVER.resolveByFirstTypeWrapper(DataWatcher.TYPE).get(this.packetHorse1_8.getPacket()));
                ObjectWrapper horseWatcherWrapper = new ObjectWrapper(this.horseDataWatcher1_8.getDataWatcherObject());

                Map dataValues = horseWatcherWrapper.getFieldByLastType(Map.class);
                dataValues.put(10, DataWatcher.V1_8.newWatchableObject(0, 10, (byte) 1));

                List<Integer> toRemove = new ArrayList<>();

                for (Object object : dataValues.values()) {

                    if (object != null) {
                        int index = DataWatcher.V1_8.getWatchableObjectIndex(object);
                        if (index == 2) {
                            DataWatcher.V1_8.setWatchableObjectValue(object, defaultName);
                        } else if (index != 3) {
                            toRemove.add(index);
                        }
                    }
                }

                toRemove.forEach(dataValues::remove);

                dataValues.put(0, DataWatcher.V1_8.newWatchableObject(0, 0, (byte) 32));

            }

            this.packetWitherSkull = new PacketWrapper(PACKET_SPAWN_CONSTRUCTOR.newInstance(witherSkull.getObject(), EntityType.WITHER_SKULL.getTypeId()));
            this.packetWitherSkull.setPacketValue("j", 66);

            this.attachPacket = new PacketWrapper(PACKET_ATTACH_CONTRUCTOR.newInstance(0, horse1_7.getObject(), witherSkull.getObject()));
            this.attachPacket.setPacketValue("b", this.entityIds[1]);
            this.attachPacket.setPacketValue("c", this.entityIds[0]);

            this.packetTeleportSkull = this.buildTeleportPacket(this.entityIds[0], location.clone().add(0, 54.56D, 0), true);
            this.packetTeleportHorse1_7 = this.buildTeleportPacket(this.entityIds[1], location.clone().add(0, 54.56D, 0), true);
            this.packetTeleportHorse1_8 = this.buildTeleportPacket(this.entityIds[2], location.clone().add(0, -2.25D, 0), true);

            this.ridingAttachPacket = PacketWrapper.createByPacketName("PacketPlayOutAttachEntity");
            this.ridingAttachPacket.setPacketValue("a", 0);
            this.ridingAttachPacket.setPacketValue("b", this.entityIds[0]);
            this.ridingAttachPacket.setPacketValue("c", this.hologram.getAttachedTo() != null ? this.hologram.getAttachedTo().getEntityId() : -1);

            this.ridingEjectPacket = PacketWrapper.createByPacketName("PacketPlayOutAttachEntity");
            this.ridingEjectPacket.setPacketValue("a", 0);
            this.ridingEjectPacket.setPacketValue("b", this.entityIds[0]);
            this.ridingEjectPacket.setPacketValue("c", -1);

            if (!rebuild) {
                this.packetDestroy = new PacketWrapper(PACKET_DESTORY_CONSTRUCTOR.newInstance(this.entityIds));
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    private PacketWrapper buildTeleportPacket(int id, Location location, boolean onGround) {
        PacketWrapper packet = PacketWrapper.createByPacketName("PacketPlayOutEntityTeleport");

        packet.setPacketValue("a", id);
        packet.setPacketValue("b", (int) (location.getX() * 32.0D));
        packet.setPacketValue("c", (int) (location.getY() * 32.0D));
        packet.setPacketValue("d", (int) (location.getZ() * 32.0D));

        packet.setPacketValue("e", (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        packet.setPacketValue("f", (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        return packet;
    }

    protected void sendSpawnPacket(Collection<? extends Player> players) {

        players.forEach(player -> {

            if (SpigotUtil.getProtocolVersion(player) > 5) {
                MinecraftReflection.sendPacket(player, this.packetHorse1_8);
            } else {
                MinecraftReflection.sendPacket(player, this.packetHorse1_7);
                MinecraftReflection.sendPacket(player, this.packetWitherSkull);
                MinecraftReflection.sendPacket(player, this.attachPacket);
            }

        });

    }

    protected void sendTeleportPacket(Collection<? extends Player> players) {
        players.forEach(player -> {

            if (SpigotUtil.getProtocolVersion(player) > 5) {
                MinecraftReflection.sendPacket(player,  packetTeleportHorse1_8);
            } else {
                MinecraftReflection.sendPacket(player, this.packetTeleportHorse1_7);
                MinecraftReflection.sendPacket(player, this.packetTeleportSkull);
            }

        });
    }

    protected void sendNamePackets(Collection<? extends Player> players) {
        players.forEach(player -> {
            int protocol = SpigotUtil.getProtocolVersion(player);

            int id = protocol > 5 ? this.entityIds[2] : this.entityIds[1];
            DataWatcherWrapper dataWatcher = protocol > 5 ? this.horseDataWatcher1_8 : this.horseDataWatcher1_7;

            String name = this.viewHandler.view(player);
            PacketWrapper packet = this.buildNamePacket(id, dataWatcher, 2, 3, name);

            MinecraftReflection.sendPacket(player, packet);

            if (protocol <= 5
                && this.entityIds.length > 1) {

                MinecraftReflection.sendPacket(player, this.buildNamePacket(this.entityIds[1], this.horseDataWatcher1_7, 10, 11, name));

            }
        });
    }

    protected void sendDestroyPacket(Collection<? extends Player> players) {
        players.forEach(player -> MinecraftReflection.sendPacket(player, this.packetDestroy));
    }

    protected void sendAttachPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (this.hologram.isAttached()) {
                MinecraftReflection.sendPacket(player, this.ridingEjectPacket);
            } else {
                MinecraftReflection.sendPacket(player, this.ridingAttachPacket);
            }
        });
    }

    private PacketWrapper packetDestroy;
    private PacketWrapper packetTeleportSkull, packetTeleportHorse1_7, packetTeleportHorse1_8;
    private PacketWrapper packetWitherSkull;
    private PacketWrapper packetHorse1_7, packetHorse1_8;
    private PacketWrapper attachPacket, ridingAttachPacket, ridingEjectPacket;
    private DataWatcherWrapper witherSkullDataWatcher, horseDataWatcher1_7, horseDataWatcher1_8;

    private PacketWrapper buildNamePacket(int id, DataWatcherWrapper dataWatcher, int nameIndex, int visibilityIndex, String name) {
        dataWatcher.setValue(nameIndex, DataWatcher.V1_9.ValueType.ENTITY_NAME, name != null ? name : "");
        dataWatcher.setValue(visibilityIndex, DataWatcher.V1_9.ValueType.ENTITY_NAME_VISIBLE, (byte) (name != null && !name.isEmpty() ? 1 : 0));

        return new PacketWrapper(PACKET_METADATA_CONSTRUCTOR.newInstance(id, dataWatcher.getDataWatcherObject(), true));
    }

    private static FieldResolver ENTITY_FIELD_RESOLVER;
    private static MethodResolver ENTITY_METHOD_RESOLVER;
    private static FieldWrapper<Double> LOC_X_FIELD;
    private static FieldWrapper<Double> LOC_Y_FIELD;
    private static FieldWrapper<Double> LOC_Z_FIELD;

    static {

        try {
            Class<?> entityType = CLASS_RESOLVER.resolve("Entity");
            ENTITY_FIELD_RESOLVER = new FieldResolver(entityType);
            ENTITY_METHOD_RESOLVER = new MethodResolver(entityType);

            LOC_X_FIELD = ENTITY_FIELD_RESOLVER.resolveWrapper("locX");
            LOC_Y_FIELD = ENTITY_FIELD_RESOLVER.resolveWrapper("locY");
            LOC_Z_FIELD = ENTITY_FIELD_RESOLVER.resolveWrapper("locZ");
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    private ObjectWrapper buildWitherSkull(Location location) {
        ObjectWrapper entityWitherSkull = new ObjectWrapper(ENTITY_WITHER_SKULL_CONSTRUCTOR.newInstanceSilent(MinecraftReflection.getHandleSilent(location.getWorld())));
        LOC_X_FIELD.set(entityWitherSkull.getObject(), location.getX());
        LOC_Y_FIELD.set(entityWitherSkull.getObject(), location.getY());
        LOC_Z_FIELD.set(entityWitherSkull.getObject(), location.getZ());
        return entityWitherSkull;
    }

    private ObjectWrapper buildHorse(Location location, boolean datawatcher, String defaultName) {

        Object world = MinecraftReflection.getHandleSilent(location.getWorld());

        ObjectWrapper entityHorse = new ObjectWrapper(ENTITY_HORSE_CONSTRUCTOR.newInstance(world));

        LOC_X_FIELD.set(entityHorse.getObject(), location.getX());
        LOC_Y_FIELD.set(entityHorse.getObject(), location.getY());
        LOC_Z_FIELD.set(entityHorse.getObject(), location.getZ());

        entityHorse.invoke("setCustomName", defaultName);
        entityHorse.getMethod("setCustomNameVisible", boolean.class).invoke(entityHorse.getObject(), !datawatcher || (defaultName != null && !defaultName.isEmpty()));

        if (datawatcher) {
            entityHorse.invoke("setAge", -1700000);
            Object dataWatcher = entityHorse.invoke("getDataWatcher");

            CommonUtility.tryCatch(() -> DataWatcher.setValue(dataWatcher, 12, DataWatcher.V1_9.ValueType.ENTITY_FLAG, (byte) 96));
        }

        return entityHorse;
    }

}
