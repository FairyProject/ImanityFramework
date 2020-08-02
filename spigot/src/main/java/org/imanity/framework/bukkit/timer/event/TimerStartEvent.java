package org.imanity.framework.bukkit.timer.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.imanity.framework.bukkit.timer.Timer;

@Getter
@Setter
public class TimerStartEvent extends TimerEvent implements Cancellable {

    private static HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;

    public TimerStartEvent(Timer timer) {
        super(timer);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }


}
