package org.imanity.framework.bukkit.visual.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.imanity.framework.bukkit.visual.VisualBlockClaim;

@Getter
@Setter
public class PreHandleVisualClaimEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;
    private VisualBlockClaim claim;

    public PreHandleVisualClaimEvent(Player who, VisualBlockClaim claim) {
        super(who);
        this.claim = claim;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


}
