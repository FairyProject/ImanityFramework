package org.imanity.framework.bukkit.packet.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.imanity.framework.bukkit.packet.PacketService;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.plugin.service.Autowired;

@Component
public class PacketPlayerListener implements Listener {

    @Autowired
    private PacketService packetService;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        this.packetService.inject(player);
    }

}
