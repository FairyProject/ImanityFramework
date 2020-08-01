package org.imanity.framework.timer.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.imanity.framework.timer.Timer;

@Getter
public class TimerExtendEvent extends TimerEvent implements Cancellable {

    private static HandlerList HANDLER_LIST = new HandlerList();

    @Setter
    private boolean cancelled;
    private long oldDuration;
    private long newDuration;
    @Setter
    private long extended;

    public TimerExtendEvent(Timer timer, long oldDuration, long newDuration, long extended) {
        super(timer);
        this.oldDuration = oldDuration;
        this.newDuration = newDuration;
        this.extended = extended;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
