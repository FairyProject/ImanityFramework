package org.imanity.framework.boot.task;

import org.imanity.framework.task.ITaskScheduler;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskScheduler implements ITaskScheduler {

    private final AtomicInteger id = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, AsyncTask> tasks = new ConcurrentHashMap<>();
//    private final

    private final ScheduledExecutorService executorService;

    public AsyncTaskScheduler() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(() -> {

            for (AsyncTask task : this.tasks.values()) {
                task.setNext(task.getNext() - 1);
                if (task.getNext() == 0) {
                    task.getRunnable().run();

                    if (task.getPeriod() < 1) {

                    }
                }
            }
        }, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public int runAsync(Runnable runnable) {
        return this.handle(0L, -1L, runnable);
    }

    @Override
    public int runAsyncScheduled(Runnable runnable, long time) {
        return this.handle(time, -1L, runnable);
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long time) {
        return this.handle(time, runnable);
    }

    @Override
    public int runAsyncRepeated(Runnable runnable, long delay, long time) {
        return this.handle(delay, time, runnable);
    }

    @Override
    public int runSync(Runnable runnable) {
        return this.runAsync(runnable);
    }

    @Override
    public int runScheduled(Runnable runnable, long time) {
        return this.runAsyncScheduled(runnable, time);
    }

    @Override
    public int runRepeated(Runnable runnable, long time) {
        return this.runAsyncRepeated(runnable, time);
    }

    @Override
    public int runRepeated(Runnable runnable, long delay, long time) {
        return this.runAsyncRepeated(runnable, delay, time);
    }

    public int handle(long period, Runnable runnable) {
        return this.handle(period, runnable);
    }

    public int handle(long next, long period, Runnable runnable) {
        int id = this.id.getAndIncrement();
        this.tasks.put(id, new AsyncTask(id, next, period, runnable));

        return id;
    }

    @Override
    public void cancel(int taskId) {
        this.tasks.remove(taskId);
    }
}
