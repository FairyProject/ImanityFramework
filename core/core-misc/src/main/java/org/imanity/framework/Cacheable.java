package org.imanity.framework;

import org.imanity.framework.cache.Unless;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Makes a method response cacheable in memory for some time.
 *
 * <p>For example, this {@code load()} method loads some data from the network
 * and we want it to cache loaded data for 5 seconds (to avoid delays):
 *
 * <pre> &#64;Cacheable(lifetime = 5, unit = TimeUnit.SECONDS)
 * String load(String resource) throws IOException {
 *   return "something";
 * }</pre>
 *
 * <p>You can cache them forever, which means that once calculated and
 * cached value will never expire (may be a nice alternative to static
 * initializers):
 *
 * <pre> &#64;Cacheable(forever = true)
 * String load(String resource) throws IOException {
 *   return "something";
 * }</pre>
 *
 * <p>Since version 0.7.14 you can also annotate methods that should flush
 * cache of the object.
 *
 * <p>Since 0.7.18 you can control when exactly flushing happens, with
 * {@link ClearBefore} and {@link ClearAfter} annotations
 * ({@link Cacheable.Flush} is deprecated), for example:
 *
 * <pre>public class Page {
 *   &#64;Cacheable
 *   public String get() {
 *     // load data from external source, e.g. the network
 *     return data;
 *   }
 *   &#64;Cacheable.FlushBefore
 *   public void set(String data) {
 *     // save data to the network
 *   }
 * }</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id: 9b6a5b2897e06cef755d08d7cf2896f5aa84c1df $
 * @since 0.7.7
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    /**
     * Lifetime of an object in cache, in time units.
     */
    int lifetime() default 1;

    /**
     * Time units of object lifetime.
     *
     * <p>The minimum unit you can use is a second. We simply can't cache for
     * less than a second, because cache is being cleaned every second.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Keep in cache forever.
     */
    boolean forever() default false;

    /**
     * Returns the current store after the expiration, and
     * then asynchronously update the data.
     */
    boolean asyncUpdate() default false;

    /**
     * Storing the key of the cacheable
     *
     */
    String key() default "";

    /**
     * Ignore if the key has anything appears null
     *
     */
    boolean ignoreKeyNull() default false;

    /**
     * Don't store if unless returns true
     *
     */
    Class<? extends Unless>[] unless() default { };

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, before being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearBefore {
    }

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, after being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ClearAfter {
    }

}
