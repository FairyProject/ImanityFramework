package org.imanity.framework.task;

public interface ITaskScheduler {

    int runAsync(Runnable runnable);

    int runAsyncScheduled(Runnable runnable, long time);

    int runAsyncRepeated(Runnable runnable, long time);

    int runSync(Runnable runnable);

    int runScheduled(Runnable runnable, long time);

    int runRepeated(Runnable runnable, long time);

    void cancel(int taskId);

}
