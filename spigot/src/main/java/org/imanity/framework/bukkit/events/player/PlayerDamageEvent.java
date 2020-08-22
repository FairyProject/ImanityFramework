package org.imanity.framework.bukkit.events.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;

public class PlayerDamageEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    @Getter
    private EntityDamageEvent entityDamageEvent;

    public PlayerDamageEvent(Player player, EntityDamageEvent entityDamageEvent) {
        super(player);
        this.entityDamageEvent = entityDamageEvent;
    }

    @Override
    public void setCancelled(boolean b) {
        this.entityDamageEvent.setCancelled(b);
    }

    @Override
    public boolean isCancelled() {
        return this.entityDamageEvent.isCancelled();
    }

    public void setDamage(double damage) {
        this.entityDamageEvent.setDamage(damage);
    }

    public double getDamage() {
        return this.entityDamageEvent.getDamage();
    }

    public double getFinalDamage() {
        return this.entityDamageEvent.getFinalDamage();
    }

    public EntityDamageEvent.DamageCause getCause() {
        return this.entityDamageEvent.getCause();
    }
}
