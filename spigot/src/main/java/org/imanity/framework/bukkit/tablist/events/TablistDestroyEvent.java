package org.imanity.framework.bukkit.tablist.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TablistDestroyEvent extends Event {

    @Getter public static HandlerList handlerList = new HandlerList();

    public TablistDestroyEvent() {

    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
