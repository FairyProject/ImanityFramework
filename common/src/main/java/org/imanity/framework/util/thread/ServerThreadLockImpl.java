package org.imanity.framework.util.thread;

import org.imanity.framework.ImanityCommon;

import java.util.concurrent.CountDownLatch;

public class ServerThreadLockImpl implements ServerThreadLock {
    // used to mark when the main thread has been obtained and blocked
    private final CountDownLatch obtainedSignal = new CountDownLatch(1);

    // used to mark when the lock is closed
    private final CountDownLatch doneSignal = new CountDownLatch(1);

    ServerThreadLockImpl() {
        // already sync - just countdown on obtained & return
        if (ImanityCommon.BRIDGE.isServerThread()) {
            this.obtainedSignal.countDown();
            return;
        }

        // synchronize with the main thread, then countdown
        ImanityCommon.TASK_SCHEDULER.runSync(this::signal);

        // wait for the main thread to become synchronized
        await();
    }

    @Override
    public void close() {
        // mark that the work has been completed, and unblock the main thread
        this.doneSignal.countDown();
    }

    private void await() {
        // await sync with the server thread
        try {
            this.obtainedSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void signal() {
        // we're on the main thread now
        // firstly, countdown the obtained latched so the blocked code can start to execute
        this.obtainedSignal.countDown();

        // then block the main thread & wait for the executed code to run
        try {
            this.doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
