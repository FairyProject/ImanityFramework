package org.imanity.framework.boot.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class AsyncTask {

    private final int id;

    @Setter
    private volatile long next;
    private final long period;

    private final Runnable runnable;

}
