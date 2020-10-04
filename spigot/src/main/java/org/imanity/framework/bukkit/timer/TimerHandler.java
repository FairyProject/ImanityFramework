package org.imanity.framework.bukkit.timer;

import com.google.common.collect.ImmutableList;
import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.timer.event.TimerStartEvent;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service(name = "timer")
public class TimerHandler implements Runnable, IService {

    private List<Timer> timers;

    private BukkitTask task;

    public void init() {
        this.timers = new ArrayList<>();
        this.task = TaskUtil.runRepeated(this, 5L);
    }

    @Override
    public void stop() {
        this.task.cancel();
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

    public synchronized void clear(Timer timer) {
        this.timers.remove(timer);
    }

    public synchronized void clear(Class<? extends Timer> timerClass) {
        this.timers.removeIf(timerClass::isInstance);
    }

    public boolean isTimerRunning(Class<? extends Timer> timerClass) {
        return this.getTimer(timerClass) != null;
    }

    public synchronized <T extends Timer> T getTimer(Class<T> timerClass) {
        return (T) this.timers
                .stream()
                .filter(timerClass::isInstance)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void run() {

        synchronized (this.timers) {
            Iterator<Timer> iterator = this.timers.iterator();

            while (iterator.hasNext()) {
                Timer timer = iterator.next();
                if (!timer.isPaused()) {

                    timer.tick();
                    if (timer.isTimerElapsed() && timer.finish()) {
                        timer.clear(false);
                        iterator.remove();
                    }
                }
            }
        }
    }
}
