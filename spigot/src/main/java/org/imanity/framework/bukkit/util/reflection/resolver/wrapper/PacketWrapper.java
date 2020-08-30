package org.imanity.framework.bukkit.util.reflection.resolver.wrapper;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;

public class PacketWrapper extends WrapperAbstract {

    private static final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

    public static PacketWrapper createByPacketName(String packetName) {
        try {
            ClassWrapper<?> classWrapper = new ClassWrapper<>(CLASS_RESOLVER.resolve(packetName));
            Object packet = classWrapper.newInstance();

            return new PacketWrapper(packet);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unexpected error while creating packet wrapper", throwable);
        }
    }

    private final Object packetObject;
    private final FieldResolver fieldResolver;

    public PacketWrapper(Object packetObject) {
        this.packetObject = packetObject;

        this.fieldResolver = new FieldResolver(this.packetObject.getClass());
    }

    public Object getPacket() {
        return packetObject;
    }

    public void sendPacket(Player player) {
        MinecraftReflection.sendPacket(player, this.packetObject);
    }

    public void setPacketValue(String field, Object value) {
        try {
            fieldResolver.resolve(field).set(getPacket(), value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getPacketValue(String field) {
        Object value = null;
        try {
            value = fieldResolver.resolve(field).get(getPacket());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public boolean exists() {
        return this.packetObject != null;
    }
}
