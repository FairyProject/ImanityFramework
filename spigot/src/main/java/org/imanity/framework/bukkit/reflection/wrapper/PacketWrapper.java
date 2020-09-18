package org.imanity.framework.bukkit.reflection.wrapper;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;

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

    private boolean cached;

    public PacketWrapper(Object packetObject) {
        this.packetObject = packetObject;

        this.fieldResolver = new FieldResolver(this.packetObject.getClass());
        this.cached = true;
    }

    public PacketWrapper noCache() {
        this.cached = false;
        return this;
    }

    public Object getPacket() {
        return packetObject;
    }

    public void sendPacket(Player player) {
        MinecraftReflection.sendPacket(player, this.packetObject);
    }

    public PacketWrapper setPacketValueByType(Class<?> type, Object value) {
        try {
            FieldWrapper<Object> fieldWrapper = fieldResolver.resolveByFirstTypeWrapper(type);
            fieldWrapper.set(this.getPacket(), value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return this;
    }

    public PacketWrapper setPacketValue(String field, Object value) {
        try {
            fieldResolver.resolve(field).set(getPacket(), value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
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

    public <T> FieldWrapper<T> getFieldWrapperByIndex(Class<T> type, int index) {
        return this.fieldResolver.resolve(type, index);
    }

    public void setFieldByIndex(Class type, int index, Object value) {
        this.fieldResolver.resolve(type, index).set(this.packetObject, value);
    }

    public <T> T getPacketValueByIndex(Class<T> type, int index) {
        return this.getFieldWrapperByIndex(type, index).get(this.packetObject);
    }

    @Override
    public boolean exists() {
        return this.packetObject != null;
    }
}
