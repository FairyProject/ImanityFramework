package org.imanity.framework.bukkit.reflection.wrapper;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.util.AccessUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
    private final Map<Class<?>, Map<Integer, Field>> fieldIndexes;

    private boolean cached;

    @SneakyThrows
    public PacketWrapper(Object packetObject) {
        this.packetObject = packetObject;

        this.fieldResolver = new FieldResolver(this.packetObject.getClass());
        FieldAccess fieldAccess = FieldAccess.get(this.packetObject.getClass());

        this.fieldIndexes = new HashMap<>();

        for (Field field : fieldAccess.getFields()) {
            Map<Integer, Field> fields = this.fieldIndexes.computeIfAbsent(field.getType(), (clazz) -> Maps.newHashMap());

            fields.put(fields.size(), AccessUtil.setAccessible(field));
            this.fieldIndexes.put(field.getType(), fields);
        }

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

    public <T> Field getFieldByIndex(Class<T> type, int index) {
        Map<Integer, Field> typeFields = this.fieldIndexes.getOrDefault(type, null);
        if (typeFields == null || typeFields.isEmpty()) return null;

        return typeFields.getOrDefault(index, null);
    }

    @SneakyThrows
    public void setFieldByIndex(Class<?> type, int index, Object value) {
        Field field = this.getFieldByIndex(type, index);
        if (field == null) throw new RuntimeException("The field attempted to fetch with type " + type.getSimpleName() + " and index " + index + " on packet class " + this.packetObject.getClass().getSimpleName() + " does not exist");

        field.set(this.packetObject, value);
    }

    @SneakyThrows
    public <T> T getPacketValueByIndex(Class<T> type, int index) {
        Field field = this.getFieldByIndex(type, index);
        if (field == null) throw new RuntimeException("The field attempted to fetch with type " + type.getSimpleName() + " and index " + index + " on packet class " + this.packetObject.getClass().getSimpleName() + " does not exist");

        return (T) field.get(this.packetObject);
    }

    @Override
    public boolean exists() {
        return this.packetObject != null;
    }
}
