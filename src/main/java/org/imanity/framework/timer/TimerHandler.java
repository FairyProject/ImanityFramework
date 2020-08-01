package org.imanity.framework.timer;

import org.imanity.framework.Imanity;
import org.imanity.framework.timer.event.TimerStartEvent;
import org.imanity.framework.util.TaskUtil;

import java.util.ArrayList;
import java.util.List;

public class TimerHandler implements Runnable {

    private List<Timer> timers = new ArrayList<>();

    public void init() {
        TaskUtil.runAsyncRepeated(this, 5L);
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

    @Override
    public void run() {
        List<Timer> toRemove = null;

        for (Timer timer : this.timers) {
            if (!timer.isPaused()) {

                timer.tick();
                if (timer.isTimerElapsed() && timer.finish()) {
                    if (toRemove == null) {
                        toRemove = new ArrayList<>();
                    }
                    toRemove.add(timer);
                }

            }
        }

        if (toRemove != null) {
            List<Timer> finalToRemove = toRemove;
            TaskUtil.runSync(() -> this.timers.removeAll(finalToRemove));
        }
    }
}
