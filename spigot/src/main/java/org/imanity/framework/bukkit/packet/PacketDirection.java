package org.imanity.framework.bukkit.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.util.reflection.resolver.ConstructorResolver;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public enum PacketDirection {

    READ,
    WRITE;

    private final Multimap<Byte, PacketListener> registeredPacketListeners = HashMultimap.create();
    private Map<Byte, Class<? extends WrappedPacket>> typeToWrappedPacket;

    public void register(Map<Byte, Class<? extends WrappedPacket>> typeToWrappedPacket) {
        if (this.typeToWrappedPacket != null) {
            throw new IllegalStateException("The Wrapped Packet are already registered!");
        }
        this.typeToWrappedPacket = typeToWrappedPacket;
    }

    public byte getPacketType(Object packet) {

        switch (this) {

            case READ:
                return PacketType.Client.getIdByType(packet.getClass());

            case WRITE:
                return PacketType.Server.getIdByType(packet.getClass());

        }

        return -1;

    }

    public boolean isPacketListening(byte id) {
        return this.registeredPacketListeners.containsKey(id);
    }

    public WrappedPacket getWrappedFromNMS(Player player, byte id, Object packet) {

        Class<? extends WrappedPacket> wrappedPacketClass = this.typeToWrappedPacket.getOrDefault(id, null);

        if (wrappedPacketClass == null) {
            return new WrappedPacket(player, packet);
        }

        return (WrappedPacket) new ConstructorResolver(wrappedPacketClass)
                .resolveWrapper(
                        new Class[] { Player.class, Object.class },
                        new Class[] { Object.class })
                .resolveBunch(
                        new Object[] { player, packet },
                        new Object[] { packet }
                );
    }

    public WrappedPacket getWrappedFromNMS(Player player, byte id) {

        Class<? extends WrappedPacket> wrappedPacketClass = this.typeToWrappedPacket.getOrDefault(id, null);

        if (wrappedPacketClass == null) {
            return new WrappedPacket(player);
        }

        return (WrappedPacket)  new ConstructorResolver(wrappedPacketClass)
                .resolveWrapper(
                        new Class[] { Player.class },
                        new Class[0])
                .resolveBunch(
                        new Object[] { player },
                        new Object[0]
                );

    }

}
