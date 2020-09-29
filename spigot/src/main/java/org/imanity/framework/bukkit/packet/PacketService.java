package org.imanity.framework.bukkit.packet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.packet.netty.INettyInjection;
import org.imanity.framework.bukkit.packet.netty.NettyInjection1_8;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.PacketContainer;
import org.imanity.framework.bukkit.packet.wrapper.SendableWrapper;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.plugin.service.Autowired;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;
import org.imanity.framework.util.FileUtils;
import org.imanity.framework.util.annotation.AnnotationDetector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Service(name = "packet")
public class PacketService implements IService {

    public static final String CHANNEL_HANDLER = ImanityCommon.METADATA_PREFIX + "ChannelHandler";

    @Autowired
    private static PacketService INSTANCE;

    public static void send(Player player, SendableWrapper sendableWrapper) {
        PacketService.INSTANCE.sendPacket(player, sendableWrapper);
    }

    private final Multimap<Class<?>, PacketListener> registeredPacketListeners = HashMultimap.create();

    @Getter
    private INettyInjection nettyInjection;

    @Override
    public void init() {

        try {

            Class.forName("io.netty.channel.Channel");
            nettyInjection = new NettyInjection1_8();

        } catch (ClassNotFoundException ex) {

//            nettyInjection = new NettyInjection1_7();

        }

        PacketTypeClasses.load();
        WrappedPacket.init();

        try {
            nettyInjection.registerChannels();
        } catch (Throwable throwable) {
            Imanity.LOGGER.info("Late Bind was enabled, late inject channels.");
            TaskUtil.runScheduled(() -> {
                try {
                    nettyInjection.registerChannels();
                } catch (Throwable throwable1) {
                    throw new RuntimeException(throwable1);
                }
            }, 0L);
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
        this.nettyInjection.unregisterChannels();
    }

    public void registerPacketListener(PacketListener packetListener) {
        for (Class<?> type : packetListener.type()) {
            if (type == null) {
                throw new UnsupportedOperationException("There is one packet doesn't exists in current version!");
            }

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
        Class<?> type = packet.getClass();

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.READ.getWrappedFromNMS(player, PacketType.Client.getIdByType(type), packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.read(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

    public Object write(Player player, Object packet) {
        Class<?> type = packet.getClass();

        if (!this.registeredPacketListeners.containsKey(type)) {
            return packet;
        }

        WrappedPacket wrappedPacket = PacketDirection.WRITE.getWrappedFromNMS(player, PacketType.Server.getIdByType(type), packet);

        boolean cancelled = false;
        for (PacketListener packetListener : this.registeredPacketListeners.get(type)) {
            if (!packetListener.write(player, wrappedPacket)) {
                cancelled = true;
            }
        }

        return cancelled ? null : packet;
    }

    public void sendPacket(Player player, SendableWrapper packet) {
        PacketContainer packetContainer = packet.asPacketContainer();
        MinecraftReflection.sendPacket(player, packetContainer.getMainPacket());

        for (Object extra : packetContainer.getExtraPackets()) {
            MinecraftReflection.sendPacket(player, extra);
        }
    }

}
