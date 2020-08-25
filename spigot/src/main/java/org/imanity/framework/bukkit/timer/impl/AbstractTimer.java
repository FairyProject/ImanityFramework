package org.imanity.framework.bukkit.timer.impl;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.timer.Timer;
import org.imanity.framework.bukkit.timer.event.TimerElapsedEvent;
import org.imanity.framework.bukkit.timer.event.TimerExtendEvent;
import org.imanity.framework.bukkit.util.CountdownData;
import org.imanity.framework.bukkit.util.RV;
import org.imanity.framework.bukkit.util.Utility;

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
        this.elapsedTime = this.beginTime + this.duration;
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
        return (int) Math.ceil(this.timeRemaining() / 1000D);
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

    public void sendMessage(Player player, String message, int seconds) {
        player.sendMessage(message);
    }

    public String getScoreboardText(Player player) {
        return "&fTimer: &e" + this.secondsRemaining() + "s";
    }

    public void clear() {
        Imanity.TIMER_HANDLER.clear(this);
    }

    @Override
    public void tick() {

        int seconds = this.secondsRemaining();
        if (countdownData != null &&
                !this.countdownData.isEnded()
            && this.countdownData.canAnnounce(seconds)) {

            Collection<? extends Player> players = this.getReceivers();
            if (players != null) {
                players.forEach(player -> this.sendMessage(player, Utility.replace(this.announceMessage(player, seconds),
                        RV.o("<player>", player.getName()),
                        RV.o("<seconds>", seconds)
                ), seconds));
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
