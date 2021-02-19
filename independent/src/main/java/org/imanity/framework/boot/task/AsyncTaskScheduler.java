/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.boot.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.task.ITaskScheduler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncTaskScheduler implements ITaskScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final AtomicInteger id = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, AsyncTask> tasks = new ConcurrentHashMap<>();
    private final AtomicBoolean changed = new AtomicBoolean(false);

    private final Queue<AsyncTask> pending = new ArrayDeque<>();
    private final List<AsyncTask> temp = new ArrayList<>();

    private final ScheduledExecutorService executorService;

    public AsyncTaskScheduler() {
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Task Scheduler Thread")
                .setUncaughtExceptionHandler((thread, throwable) -> LOGGER.error(throwable))
                .build()
        );
        this.executorService.scheduleAtFixedRate(() -> {

            if (this.changed.get()) {
                this.parsePending();
            }

            AsyncTask task;
            while ((task = this.pending.poll()) != null) {
                task.setNext(task.getNext() - 1);
                if (task.getNext() == 0) {
                    task.getRunnable().run();

                    if (task.getPeriod() < 1) {
                        this.tasks.remove(task.getId());
                    } else {
                        task.setNext(task.getPeriod());
                        this.temp.add(task);
                    }
                }
            }

            this.pending.addAll(this.temp);
            this.temp.clear();

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

    private void parsePending() {
        this.pending.clear();
        this.pending.addAll(this.tasks.values());
    }

    public int handle(long period, Runnable runnable) {
        return this.handle(period, period, runnable);
    }

    public int handle(long next, long period, Runnable runnable) {
        int id = this.id.getAndIncrement();
        this.tasks.put(id, new AsyncTask(id, next, period, runnable));
        this.changed.set(true);

        return id;
    }

    public void shutdown() {
        this.executorService.shutdown();
        while (!this.executorService.isTerminated()) {
            try {
                this.executorService.awaitTermination(30L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel(int taskId) {
        this.tasks.remove(taskId);
        this.changed.set(true);
    }
}
