package org.imanity.frameworktest;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.imanity.framework.CacheEvict;
import org.imanity.framework.CachePut;
import org.imanity.framework.Cacheable;
import org.imanity.framework.cache.EnableOwnCacheManager;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.*;

public class CacheableTest {

    private static final Random RANDOM = new SecureRandom();

    @Test
    public void cacheSimpleCall() {
        CacheableTest.Foo foo = new Foo(1L);
        String first = foo.get().toString();
        MatcherAssert.assertThat(first, CoreMatchers.equalTo(foo.get().toString()));
        foo.flush();
        MatcherAssert.assertThat(
                foo.get().toString(),
                CoreMatchers.not(CoreMatchers.equalTo(first))
        );
    }

    @Test
    public void asyncUpdateCacheSimpleCall() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.asyncGet().toString();
        MatcherAssert.assertThat(
                first,
                CoreMatchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2);
        MatcherAssert.assertThat(
                first,
                CoreMatchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2);
        MatcherAssert.assertThat(
                first,
                CoreMatchers.not(CoreMatchers.equalTo(foo.asyncGet().toString()))
        );
    }

    @Test
    public void cachesSimpleStaticCall() throws Exception {
        final String first = CacheableTest.Foo.staticGet();
        MatcherAssert.assertThat(
                first,
                CoreMatchers.equalTo(CacheableTest.Foo.staticGet())
        );
        CacheableTest.Foo.staticFlush();
        MatcherAssert.assertThat(
                CacheableTest.Foo.staticGet(),
                CoreMatchers.not(CoreMatchers.equalTo(first))
        );
    }

    @Test
    public void cleansCacheWhenExpired() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.get().toString();
        TimeUnit.SECONDS.sleep(5);
        MatcherAssert.assertThat(
                foo.get().toString(),
                CoreMatchers.not(CoreMatchers.equalTo(first))
        );
    }

    @Test
    public void cachesJustOnceInParallelThreads() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final Thread never = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        foo.never();
                    }
                }
        );
        never.start();
        final Set<String> values = new ConcurrentSkipListSet<String>();
        final int threads = Runtime.getRuntime().availableProcessors() << 1;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                start.await(1L, TimeUnit.SECONDS);
                values.add(foo.get().toString());
                done.countDown();
                return true;
            }
        };
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (int pos = 0; pos < threads; ++pos) {
                executor.submit(task);
            }
            start.countDown();
            done.await(30, TimeUnit.SECONDS);
            MatcherAssert.assertThat(values.size(), CoreMatchers.equalTo(1));
            never.interrupt();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void flushesWithStaticTrigger() throws Exception {
        final CacheableTest.Bar bar = new CacheableTest.Bar();
        MatcherAssert.assertThat(
                bar.get(),
                CoreMatchers.equalTo(bar.get())
        );
    }

    @Test
    public void testKey() throws Exception {
        Imanity imanity = new Imanity();

        int id = 2;
        long first = imanity.test(id);

        MatcherAssert.assertThat(first, CoreMatchers.equalTo(imanity.test(id)));
        long second = imanity.test(5);

        MatcherAssert.assertThat(first, CoreMatchers.not(second));

        imanity.evict(id);
        long third = imanity.test(id);

        MatcherAssert.assertThat(first, CoreMatchers.not(third));

        long dummyObjectA = imanity.test(new ImanityDummy(id));
        long dummyObjectB = imanity.test(new ImanityDummy(id));

        MatcherAssert.assertThat(dummyObjectA, CoreMatchers.equalTo(dummyObjectB));

        imanity.evict(id);

        MatcherAssert.assertThat(dummyObjectA, CoreMatchers.not(imanity.test(new ImanityDummy(id))));

        id = 3;
        long testPut = RANDOM.nextLong();

        imanity.put(id, testPut);
        MatcherAssert.assertThat(testPut, CoreMatchers.equalTo(testPut));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidKey() throws Exception {
        Imanity imanity = new Imanity();

        imanity.test();
    }

    @Test(expected = NullPointerException.class)
    public void testNullParameter() throws Exception {
        Imanity imanity = new Imanity();

        imanity.test((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotExistsField() throws Exception {
        Imanity imanity = new Imanity();

        imanity.xd(new ImanityDummy(2));
    }

    @EnableOwnCacheManager
    private static final class Imanity {

        @Cacheable(key = "test-$(arg0)")
        public long test(int id) {
            return RANDOM.nextLong();
        }

        @Cacheable(key = "test-$(arg0.id)")
        public long test(ImanityDummy dummy) {
            System.out.println("dummy: " + dummy.id);
            return RANDOM.nextLong();
        }

        @Cacheable(key = "test-$(arg0)")
        public String test() {
            return "hi";
        }

        @Cacheable(key = "test-$(arg0)")
        public String test(String text) {
            return "lol";
        }

        @CacheEvict(value = "test-$(arg0)")
        public void evict(int id) {

        }

        @CachePut(value = "test-${arg0}")
        public long put(int id, long value) {
            return value;
        }

        @CacheEvict("test-$(arg0.xd)")
        public void xd(ImanityDummy dummy) {

        }

    }

    private static final class ImanityDummy {

        private int id;

        public ImanityDummy(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ImanityDummy that = (ImanityDummy) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", ImanityDummy.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .toString();
        }
    }

    private static final class Foo {

        private final transient long number;

        Foo(final long num) {
            this.number = num;
        }

        @Override
        public int hashCode() {
            return this.get().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this;
        }

        @Override
        @Cacheable(forever = true)
        public String toString() {
            return Long.toString(this.number);
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public CacheableTest.Foo get() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS, asyncUpdate = true)
        public CacheableTest.Foo asyncGet() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public CacheableTest.Foo never() {
            try {
                TimeUnit.HOURS.sleep(1L);
            } catch (final InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            return this;
        }

        @Cacheable.ClearBefore
        public void flush() {
            // nothing to do
        }

        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public static String staticGet() {
            return Long.toString(CacheableTest.RANDOM.nextLong());
        }

        @Cacheable.ClearBefore
        public static void staticFlush() {
            // nothing to do
        }
    }

    public static final class Bar {

        @Cacheable
        public long get() {
            return CacheableTest.RANDOM.nextLong();
        }

    }

}
