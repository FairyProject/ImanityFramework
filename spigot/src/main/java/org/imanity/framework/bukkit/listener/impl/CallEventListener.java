package org.imanity.framework.bukkit.listener.impl;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.events.player.EntityDamageByPlayerEvent;
import org.imanity.framework.bukkit.events.player.PlayerDamageByEntityEvent;
import org.imanity.framework.bukkit.events.player.PlayerDamageByPlayerEvent;
import org.imanity.framework.bukkit.events.player.PlayerDamageEvent;
import org.imanity.framework.events.annotation.AutoWiredListener;

@AutoWiredListener
public class CallEventListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            if (damageByEntityEvent.getEntity() instanceof Player) {
                Player player = (Player) damageByEntityEvent.getEntity();
                if (damageByEntityEvent.getDamager() instanceof Player) {
                    Imanity.PLUGIN.getServer().getPluginManager().callEvent(new PlayerDamageByPlayerEvent(player, damageByEntityEvent));
                    return;
                }
                Imanity.PLUGIN.getServer().getPluginManager().callEvent(new PlayerDamageByEntityEvent(player, damageByEntityEvent));
                return;
            } else if (damageByEntityEvent.getDamager() instanceof Player) {
                Player player = (Player) damageByEntityEvent.getDamager();
                Imanity.PLUGIN.getServer().getPluginManager().callEvent(new EntityDamageByPlayerEvent(player, damageByEntityEvent));
            }
            return;
        }
        if (event.getEntity() instanceof Player) {
            Imanity.PLUGIN.getServer().getPluginManager().callEvent(new PlayerDamageEvent((Player) event.getEntity(), event));
        }
    }

}
