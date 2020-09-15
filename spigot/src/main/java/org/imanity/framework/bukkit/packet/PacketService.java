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
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.annotation.AnnotationDetector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

@Service(name = "packet")
public class PacketService implements IService {

    public static final String CHANNEL_HANDLER = ImanityCommon.METADATA_PREFIX + "ChannelHandler";
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

        try {

            this.loadWrappers();

        } catch (Throwable throwable) {
            throw new RuntimeException("Something wrong while loading wrapped packets", throwable);
        }
    }

    private void loadWrappers() throws Throwable {
        ImmutableMap.Builder<Byte, Class<? extends WrappedPacket>> readBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<Byte, Class<? extends WrappedPacket>> writeBuilder = ImmutableMap.builder();

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

                    switch (annotation.direction()) {
                        case READ:
                            readBuilder.put(annotation.value(), type);
                            break;
                        case WRITE:
                            writeBuilder.put(annotation.value(), type);
                            break;
                    }
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

        PacketDirection.READ.register(readBuilder.build());
        PacketDirection.WRITE.register(writeBuilder.build());
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

    public Object read(Player player, Object packet) {
        byte type = PacketDirection.READ.getPacketType(packet);

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.READ.getWrappedFromNMS(player, type, packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.read(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

    public Object write(Player player, Object packet) {
        byte type = PacketDirection.WRITE.getPacketType(packet);

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.WRITE.getWrappedFromNMS(player, type, packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.write(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

}
