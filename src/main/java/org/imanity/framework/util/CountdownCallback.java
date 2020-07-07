package org.imanity.framework.util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

public class CountdownCallback {

    private final Runnable runnable;
    private final AtomicInteger count;

    public CountdownCallback(int count, Runnable runnable) {
        this.runnable = runnable;
        this.count = new AtomicInteger(count);
    }

    private boolean done;

    public void countdown() {
        if (done) {
            return;
        }

        if (this.count.decrementAndGet() <= 0) {
            done = true;

            runnable.run();
        }
    }

}
