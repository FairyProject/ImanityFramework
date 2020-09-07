package org.imanity.framework.bukkit.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPostJoinEvent extends PlayerEvent {

    private static final HandlerList handlerlist = new HandlerList();

    public PlayerPostJoinEvent(Player who) {
        super(who);
    }

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }


}
