package org.imanity.framework.bukkit.timer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.imanity.framework.bukkit.timer.Timer;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class TimerEvent extends Event {

    private final Timer timer;

}
