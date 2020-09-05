package org.imanity.framework.bukkit.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.tablist.utils.IImanityTabImpl;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.tablist.utils.impl.NMS1_8TabImpl;
import org.imanity.framework.bukkit.tablist.utils.impl.ProtocolLibTabImpl;
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

    public void registerPlayerTablist(Player player) {
        ImanityTabHandler.getInstance().getTablists().put(player.getUniqueId(), new ImanityTablist(player));
    }

    public void removePlayerTablist(Player player) {
        Team team = player.getScoreboard().getTeam("\\u000181");
        if (team != null) {
            team.unregister();
        }

        ImanityTabHandler.getInstance().getTablists().remove(player.getUniqueId());
    }

    private void setup() {

        //Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

        //Start Thread
        this.thread = new ImanityTabThread(this);
    }

    public void stop() {

        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

    }
}
