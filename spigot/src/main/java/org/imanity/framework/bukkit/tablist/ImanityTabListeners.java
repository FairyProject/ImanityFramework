package org.imanity.framework.bukkit.tablist;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;

public class ImanityTabListeners implements Listener {

    @EventHandler(
        priority = EventPriority.LOW
    )
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        System.out.println(MinecraftReflection.getProtocol(player) + " " + ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion());

//        new BukkitRunnable() {
//            @Override
//            public void run() {
                if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
                ImanityTabHandler.getInstance().getTablists().put(event.getPlayer().getUniqueId(), new ImanityTablist(event.getPlayer()));
//
//            }
//        }.runTaskLater(Imanity.PLUGIN.getInstance().getPlugin(), 40);
    }

    @EventHandler(
            priority = EventPriority.LOW
    )
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        Team team = player.getScoreboard().getTeam("\\u000181");
        if (team != null) {
            team.unregister();
        }

        ImanityTabHandler.getInstance().getTablists().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (ImanityTabHandler.getInstance().getThread() == null) {
            return;
        }
        ImanityTabHandler.getInstance().getThread().stop();
    }
}
