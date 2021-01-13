package org.imanity.framework.bukkit.events.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.locale.player.LocaleData;

public class PlayerLocaleLoadedEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final LocaleData localeData;

    public PlayerLocaleLoadedEvent(Player who, LocaleData localeData) {
        super(who);
        this.localeData = localeData;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
