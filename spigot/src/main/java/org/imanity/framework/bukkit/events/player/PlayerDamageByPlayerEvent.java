package org.imanity.framework.bukkit.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageByPlayerEvent extends PlayerDamageEvent {

    private static final HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    public PlayerDamageByPlayerEvent(Player player, EntityDamageByEntityEvent entityDamageEvent) {
        super(player, entityDamageEvent);
    }

    public Player getDamager() {
        return (Player) ((EntityDamageByEntityEvent) this.getEntityDamageEvent()).getDamager();
    }
}
