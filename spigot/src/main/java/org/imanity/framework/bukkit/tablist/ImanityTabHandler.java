package org.imanity.framework.bukkit.tablist;

import org.bukkit.Bukkit;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.tablist.utils.IImanityTabImpl;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.tablist.utils.impl.NMS1_8TabImpl;
import org.imanity.framework.bukkit.tablist.utils.impl.ProtocolLibTabImpl;
import org.imanity.framework.bukkit.util.reflection.version.protocol.ProtocolCheck;
import org.imanity.framework.bukkit.util.reflection.version.protocol.ProtocolCheckImanitySpigot;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ImanityTabHandler {

    //Instance
    @Getter private static ImanityTabHandler instance;

    private ImanityTabAdapter adapter;
    private Map<UUID, ImanityTablist> tablists;
    private ImanityTabThread thread;
    private IImanityTabImpl implementation;

    private boolean done;

    //Tablist Ticks
    @Setter private long ticks = 20;

    public ImanityTabHandler(ImanityTabAdapter adapter) {
        if (instance != null) {
            throw new RuntimeException("ImanityTab has already been instantiated!");
        }

        instance = this;

        this.adapter = adapter;
        this.tablists = new ConcurrentHashMap<>();

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

    private void setup() {
        //Register Events
        Imanity.PLUGIN.getServer().getPluginManager().registerEvents(new ImanityTabListeners(), Imanity.PLUGIN);

        //Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.stop();
            this.thread = null;
        }

        //Start Thread
        this.thread = new ImanityTabThread(this);
    }
}
