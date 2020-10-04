package org.imanity.framework.bukkit.timer;

import org.imanity.framework.bukkit.timer.impl.AbstractTimer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;

public class TimerList<T extends AbstractTimer> extends LinkedList<T> {

    public boolean isTimerRunning(Class<? extends T> timerClass) {
        for (T timer : this) {
            if (timerClass.isInstance(timer)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public <E extends T> E getTimer(Class<E> timerClass) {
        for (T timer : this) {
            if (timerClass.isInstance(timer)) {
                return (E) timer;
            }
        }

        return null;
    }

    public boolean removeTimer(Class<? extends T> timerClass) {
        Iterator<T> iterator = this.iterator();

        while (iterator.hasNext()) {
            if (timerClass.isInstance(iterator.next())) {
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public void clear() {
        Iterator<T> iterator = this.iterator();

        while (iterator.hasNext()) {
            iterator.next().clear(true);
            iterator.remove();
        }
    }
}
