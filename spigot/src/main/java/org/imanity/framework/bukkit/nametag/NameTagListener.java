package org.imanity.framework.bukkit.nametag;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.events.player.PlayerPostJoinEvent;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.events.annotation.AutoWiredListener;
import org.imanity.framework.plugin.service.Autowired;

@AutoWiredListener
public class NameTagListener implements Listener {

    @Autowired
    private NameTagService nameTagService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerPostJoinEvent event) {
        Player player = event.getPlayer();

        this.nameTagService.initialPlayer(player);
        this.nameTagService.updateFromThirdSide(player);
        this.nameTagService.updateFromFirstSide(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Metadata
                .provideForPlayer(player)
                .remove(NameTagService.TEAM_INFO_KEY);

        this.nameTagService.disconnect(player);
    }

}
