/**
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.imanity.frameworktest;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.imanity.framework.Async;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class AsyncTest {

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void executesAsynchronously() throws Exception {
        final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        final Runnable runnable = new Runnable() {
            @Async
            @Override
            public void run() {
                queue.offer(Thread.currentThread().getName());
            }
        };
        runnable.run();
        MatcherAssert.assertThat(
            queue.poll(30, TimeUnit.SECONDS),
            CoreMatchers.allOf(
                    CoreMatchers.not(Thread.currentThread().getName()),
                    CoreMatchers.startsWith("imanity-async")
            )
        );
    }

    @Test
    public void returnsFutureValue() throws Exception {
        MatcherAssert.assertThat(
            new Foo().asyncMethodWithReturnValue()
                .get(5, TimeUnit.MINUTES),
                CoreMatchers.allOf(
                        CoreMatchers.not(Thread.currentThread().getName()),
                        CoreMatchers.startsWith("imanity-async")
            )
        );
    }

    @Test(expected = IllegalStateException.class)
    public void throwsWhenMethodDoesNotReturnVoidOrFuture() {
        new Foo().asyncMethodThatReturnsInt();
    }

    private static final class Foo {

        @Async
        public Future<String> asyncMethodWithReturnValue() {
            return new Future<String>() {
                @Override
                public boolean cancel(final boolean interruptible) {
                    return false;
                }
                @Override
                public boolean isCancelled() {
                    return false;
                }
                @Override
                public boolean isDone() {
                    return true;
                }
                @Override
                public String get() {
                    return Thread.currentThread().getName();
                }
                @Override
                public String get(final long timeout, final TimeUnit unit) {
                    return Thread.currentThread().getName();
                }
            };
        }

        @Async
        public int asyncMethodThatReturnsInt() {
            return 0;
        }
    }

}
