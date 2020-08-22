package org.imanity.framework.bukkit.events.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageByEntityEvent extends org.imanity.framework.bukkit.events.player.PlayerDamageEvent {

    private static final HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    public PlayerDamageByEntityEvent(Player player, EntityDamageByEntityEvent entityDamageEvent) {
        super(player, entityDamageEvent);
    }

    public Entity getDamager() {
        return ((EntityDamageByEntityEvent) this.getEntityDamageEvent()).getDamager();
    }
}
