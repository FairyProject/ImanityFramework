package org.imanity.framework.timer.impl;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.Imanity;
import org.imanity.framework.timer.Timer;
import org.imanity.framework.timer.event.TimerElapsedEvent;
import org.imanity.framework.timer.event.TimerExtendEvent;
import org.imanity.framework.util.CountdownData;
import org.imanity.framework.util.RV;
import org.imanity.framework.util.Utility;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractTimer implements Timer {

    private boolean paused;
    private long beginTime;
    private long duration;
    private long elapsedTime;

    private boolean shouldAnnounce;

    private CountdownData countdownData;

    public AbstractTimer(long beginTime, long duration) {
        this.beginTime = beginTime;
        this.duration = duration;
    }

    public AbstractTimer(long duration) {
        this(System.currentTimeMillis(), duration);
    }

    public void announcing(boolean shouldAnnounce) {
        if (this.shouldAnnounce = shouldAnnounce
            && countdownData == null) {
            countdownData = new CountdownData(this.secondsRemaining());
        }
    }

    public boolean isTimerElapsed() {
        return System.currentTimeMillis() > elapsedTime;
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public long timeRemaining() {
        return this.elapsedTime - System.currentTimeMillis();
    }

    @Override
    public int secondsRemaining() {
        return (int) TimeUnit.MILLISECONDS.toSeconds(this.timeRemaining());
    }

    @Override
    public void extend(long millis) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, this.duration + millis, millis);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        millis = event.getExtended();
        this.duration += millis;
        this.elapsedTime = this.beginTime + this.duration;
    }

    @Override
    public void duration(long duration) {
        TimerExtendEvent event = new TimerExtendEvent(this, this.duration, duration, duration - this.duration);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        duration = this.duration + event.getExtended();
        this.duration = duration;
        this.elapsedTime = this.beginTime + this.duration;
    }

    public String announceMessage(Player player, int seconds) {
        return "Time Remaining: <seconds>";
    }

    @Override
    public void tick() {

        int seconds = this.secondsRemaining();
        if (!this.countdownData.isEnded()
            && this.countdownData.canAnnounce(seconds)) {

            Collection<? extends Player> players = this.getReceivers();
            if (players != null) {
                players.forEach(player -> player.sendMessage(Utility.replace(this.announceMessage(player, seconds),
                        RV.o("<player>", player.getName()),
                        RV.o("<seconds>", seconds)
                )));
            }

        }

    }

    @Override
    public boolean finish() {
        TimerElapsedEvent event = new TimerElapsedEvent(this);
        Imanity.callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        this.elapsed();
        return true;
    }

    public void elapsed() {

    }
}
