package org.imanity.framework.bungee.impl;

import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.task.ITaskScheduler;

import java.util.concurrent.TimeUnit;

public class BungeeTaskScheduler implements ITaskScheduler {
    @Override
    public int runAsync(Runnable runnable) {
        return Imanity.getProxy().getScheduler().runAsync(Imanity.PLUGIN, runnable).getId();
    }

    @Override
    public int runAsyncScheduled(Runnable runnable, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, 0, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long delay, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, delay * 50, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runSync(Runnable runnable) {
        return Imanity.getProxy().getScheduler().runAsync(Imanity.PLUGIN, runnable).getId();
    }

    @Override
    public int runScheduled(Runnable runnable, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runRepeated(Runnable runnable, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, 0, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public int runRepeated(Runnable runnable, long delay, long time) {
        return Imanity.getProxy().getScheduler().schedule(Imanity.PLUGIN, runnable, delay * 50, time * 50, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public void cancel(int taskId) {
        Imanity.getProxy().getScheduler().cancel(taskId);
    }
}
