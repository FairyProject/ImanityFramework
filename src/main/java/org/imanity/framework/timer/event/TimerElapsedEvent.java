package org.imanity.framework.timer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.imanity.framework.timer.Timer;

@Getter
@Setter
public class TimerElapsedEvent extends TimerEvent implements Cancellable {

    private static HandlerList HANDLER_LIST = new HandlerList();

    public TimerElapsedEvent(Timer timer) {
        super(timer);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    private boolean cancelled;
}
