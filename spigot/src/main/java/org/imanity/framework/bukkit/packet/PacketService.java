package org.imanity.framework.bukkit.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.packet.netty.INettyInjection;
import org.imanity.framework.bukkit.packet.netty.NettyInjection1_7;
import org.imanity.framework.bukkit.packet.netty.NettyInjection1_8;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.annotation.AnnotationDetector;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Service(name = "packet")
public class PacketService implements IService {

    public static final String CHANNEL_HANDLER = ImanityCommon.METADATA_PREFIX + "ChannelHandler";
    private Map<Class<?>, Class<? extends WrappedPacket>> packetToWrapped;
    private Multimap<Byte, PacketListener> registeredPacketListeners;
    private INettyInjection nettyInjection;

    @Override
    public void init() {

        try {

            Class.forName("io.netty.channel.Channel");
            nettyInjection = new NettyInjection1_8();

        } catch (ClassNotFoundException ex) {

            nettyInjection = new NettyInjection1_7();

        }

        ImanityCommon.SERVICE_HANDLER.registerAutowired(nettyInjection);
        Imanity.getPlayers().forEach(this::inject);

        this.registeredPacketListeners = HashMultimap.create();

        try {

            this.loadWrappers();

        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while loading wrapped packets", throwable);
        }
    }

    private void loadWrappers() throws Throwable {
        ImmutableMap.Builder<Class<?>, Class<? extends WrappedPacket>> builder = ImmutableMap.builder();

        new AnnotationDetector(new AnnotationDetector.TypeReporter() {

            @Override
            public void reportTypeAnnotation(Class<? extends Annotation> annotationType, String className) {
                try {
                    Class<? extends WrappedPacket> type = (Class<? extends WrappedPacket>) Class.forName(className);

                    AutowiredWrappedPacket annotation = type.getAnnotation(AutowiredWrappedPacket.class);

                    if (annotation == null) {
                        return;
                    }

                    Method method = type.getDeclaredMethod("init");
                    method.invoke(null);

                    builder.put(WrappedPacket.NMS_CLASS_RESOLVER.resolve(annotation.type()), type);
                } catch (NoSuchMethodException ex) {
                    // Ignores
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            @Override
            public Class<? extends Annotation>[] annotations() {
                return new Class[] {AutowiredWrappedPacket.class};
            }

        }).detect(FileUtils.getSelfJar());

        this.packetToWrapped = builder.build();
    }

    @Override
    public void stop() {
        Imanity.getPlayers().forEach(this::eject);
    }

    public void registerPacketListener(PacketListener packetListener) {
        for (byte type : packetListener.type()) {
            this.registeredPacketListeners.put(type, packetListener);
        }
    }

    public void inject(Player player) {
        this.nettyInjection.inject(player);
    }

    public void eject(Player player) {
        this.nettyInjection.eject(player);
    }

    private WrappedPacket getWrappedFromNMS(Player player, Object packet) {
        Class<? extends WrappedPacket> wrappedPacketClass = this.packetToWrapped.getOrDefault(packet.getClass(), null);

        if (wrappedPacketClass == null) {
            return new WrappedPacket(player, packet);
        }

        try {
            return wrappedPacketClass
                    .getConstructor(Player.class, Object.class)
                    .newInstance(player, packet);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public Object read(Player player, Object packet) {
        byte type = PacketType.Client.getIdByType(packet.getClass());

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = this.getWrappedFromNMS(player, packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.read(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

    public Object write(Player player, Object packet) {
        byte type = PacketType.Server.getIdByType(packet.getClass());

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = this.getWrappedFromNMS(player, packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.write(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

}
