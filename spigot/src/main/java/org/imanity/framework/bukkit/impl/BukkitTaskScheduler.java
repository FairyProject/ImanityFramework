package org.imanity.framework.bukkit.impl;

import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.task.ITaskScheduler;

public class BukkitTaskScheduler implements ITaskScheduler {
    @Override
    public int runAsync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskAsynchronously(Imanity.PLUGIN, runnable).getTaskId();
    }

    @Override
    public int runAsyncScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLaterAsynchronously(Imanity.PLUGIN, runnable, time).getTaskId();
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(Imanity.PLUGIN, runnable, time, time).getTaskId();
    }

    @Override
    public int runSync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTask(Imanity.PLUGIN, runnable).getTaskId();
    }

    @Override
    public int runScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLater(Imanity.PLUGIN, runnable, time).getTaskId();
    }

    @Override
    public int runRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimer(Imanity.PLUGIN, runnable, time, time).getTaskId();
    }

    @Override
    public void cancel(int taskId) {
        Imanity.PLUGIN.getServer().getScheduler().cancelTask(taskId);
    }
}
