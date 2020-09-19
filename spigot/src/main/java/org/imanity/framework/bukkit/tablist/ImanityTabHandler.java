package org.imanity.framework.bukkit.tablist;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.packet.PacketListener;
import org.imanity.framework.bukkit.packet.PacketService;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutLogin;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.bukkit.tablist.util.IImanityTabImpl;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.tablist.util.impl.v1_8.NMS1_8TabImpl;
import org.imanity.framework.bukkit.tablist.util.impl.ProtocolLibTabImpl;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.plugin.service.Autowired;

import java.util.concurrent.*;

@Getter
public class ImanityTabHandler {

    //Instance
    @Getter private static ImanityTabHandler instance;

    private static final MetadataKey<ImanityTablist> TABLIST_KEY = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "TabList", ImanityTablist.class);

    private ImanityTabAdapter adapter;
    private ScheduledExecutorService thread;
    private IImanityTabImpl implementation;

    @Autowired
    private PacketService packetService;

    //Tablist Ticks
    @Setter private long ticks = 20;

    public ImanityTabHandler(ImanityTabAdapter adapter) {
        if (instance != null) {
            throw new RuntimeException("ImanityTab has already been instantiated!");
        }

        instance = this;

        this.adapter = adapter;

        ImanityCommon.registerAutowired(this);

        this.registerImplementation();
        this.setup();
    }

    private void registerImplementation() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            this.implementation = new ProtocolLibTabImpl();
            Imanity.PLUGIN.getLogger().info("Registered Tablist Implementation with ProtocolLib");
            return;
        }

        if (MinecraftReflection.VERSION == MinecraftReflection.Version.v1_8_R3) {

            this.implementation = new NMS1_8TabImpl();
            return;
        }
        Imanity.PLUGIN.getLogger().info("Unable to register ImanityTablist with a proper implementation");
    }

    public void registerPlayerTablist(Player player) {
        ImanityTablist tablist = new ImanityTablist(player);

        Metadata
                .provideForPlayer(player)
                .put(TABLIST_KEY, tablist);
    }

    public void removePlayerTablist(Player player) {
        Metadata
                .provideForPlayer(player)
                .remove(TABLIST_KEY);
    }

    private void setup() {

        //Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }

        // To ensure client will display 60 slots on 1.7
        if (Bukkit.getMaxPlayers() < 60) {
//            this.implementation.registerLoginListener();
            packetService.registerPacketListener(new PacketListener() {
                @Override
                public Class<?>[] type() {
                    return new Class[] { PacketTypeClasses.Server.LOGIN };
                }

                @Override
                public boolean write(Player player, WrappedPacket packet1) {
                    WrappedPacketOutLogin packet = packet1.wrap(WrappedPacketOutLogin.class);
                    packet.setMaxPlayers(60);

                    System.out.println("yo my brother!");

                    return true;
                }
            });
        }

        //Start Thread
        this.thread = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("Imanity-Tablist-Thread")
            .setDaemon(true)
            .build());

        this.thread.scheduleAtFixedRate(() -> {
            for (Player player : Imanity.getPlayers()) {
                ImanityTablist tablist = Metadata
                        .provideForPlayer(player)
                        .getOrNull(TABLIST_KEY);

                if (tablist != null) {
                    tablist.update();
                }
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public void stop() {

        if (this.thread != null) {
            this.thread.shutdown();
            this.thread = null;
        }

    }
}
