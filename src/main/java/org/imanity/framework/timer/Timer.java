package org.imanity.framework.timer;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface Timer {

    boolean isTimerElapsed();

    boolean isPaused();

    boolean finish();

    void pause();

    default void start() {

    }

    default void tick() {

    }

    default Collection<? extends Player> getReceivers() {
        return null;
    }

    long timeRemaining();

    int secondsRemaining();

    void extend(long millis);

    void duration(long duration);

}
