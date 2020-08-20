package org.imanity.framework.bukkit.timer;

import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.timer.event.TimerStartEvent;
import org.imanity.framework.bukkit.util.TaskUtil;

import java.util.ArrayList;
import java.util.List;

public class TimerHandler implements Runnable {

    private final List<Timer> timers = new ArrayList<>();

    public void init() {
        TaskUtil.runRepeated(this, 5L);
    }

    public void add(Timer timer) {
        TimerStartEvent event = new TimerStartEvent(timer);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        this.timers.add(timer);
        timer.start();
    }

    public void clear(Timer timer) {
        this.timers.remove(timer);
    }

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        return this.getTimer(timerClass) != null;
    }

    public <T extends Timer> T getTimer(Class<T> timerClass) {
        return (T) this.timers
                .stream()
                .filter(timerClass::isInstance)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run() {
        for (Timer timer : this.timers) {
            if (!timer.isPaused()) {

                timer.tick();
                if (timer.isTimerElapsed() && timer.finish()) {
                    this.timers.remove(timer);
                }

            }
        }
    }
}
