package org.imanity.framework.bukkit.tablist;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.tablist.util.IImanityTabImpl;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.tablist.util.impl.v1_8.ImanitySpigotTabImpl;
import org.imanity.framework.bukkit.tablist.util.impl.v1_8.NMS1_8TabImpl;
import org.imanity.framework.bukkit.tablist.util.impl.ProtocolLibTabImpl;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Getter
public class ImanityTabHandler {

    //Instance
    @Getter private static ImanityTabHandler instance;

    private ImanityTabAdapter adapter;
    private Map<UUID, ImanityTablist> tablists;
    private ScheduledExecutorService thread;
    private IImanityTabImpl implementation;

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

            if (SpigotUtil.SPIGOT_TYPE == SpigotUtil.SpigotType.IMANITY) {
                this.implementation = new ImanitySpigotTabImpl();
            } else {
                this.implementation = new NMS1_8TabImpl();
            }
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
            this.thread.shutdown();
            this.thread = null;
        }

        // To ensure client will display 60 slots on 1.7
        if (Bukkit.getMaxPlayers() < 60) {
            this.implementation.registerLoginListener();
        }

        //Start Thread
        this.thread = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("Imanity-Tablist-Thread")
            .setDaemon(true)
            .build());

        this.thread.scheduleAtFixedRate(() -> {
            for (ImanityTablist tablist : this.getTablists().values()) {
                tablist.update();
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
