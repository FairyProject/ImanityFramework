package org.imanity.framework.bukkit.player.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.player.data.PlayerData;

public class PlayerDataLoadEvent extends PlayerEvent {

    public static void callEvent(Player player, PlayerData playerData) {
        PlayerDataLoadEvent event = new PlayerDataLoadEvent(player, playerData);
        Imanity.PLUGIN.getServer().getPluginManager().callEvent(event);
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Getter
    private final PlayerData playerData;

    public PlayerDataLoadEvent(Player player, PlayerData playerData) {
        super(player, true);

        this.playerData = playerData;
    }
}
