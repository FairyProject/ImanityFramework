package org.imanity.framework.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.imanity.framework.CacheEvict;
import org.imanity.framework.CachePut;
import org.imanity.framework.Cacheable;
import org.imanity.framework.cache.impl.CacheKeyAbstract;
import org.imanity.framework.cache.impl.CacheKeyMethod;
import org.imanity.framework.cache.impl.CacheKeyString;
import org.imanity.framework.util.AccessUtil;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * A Modified version of AspectJ cacheable annotation
 *
 * Provide key, evict and cache managers
 *
 */
@Aspect
public class CacheableAspect {

    public static final Logger LOGGER = LogManager.getLogger(CacheableAspect.class);

    private transient final CacheManager defaultCacheManager;
    private transient final Map<Class<?>, CacheManager> cacheManagers;

    public static ScheduledExecutorService CLEANER_SERVICE;
    public static ExecutorService UPDATER_SERVICE;

    public CacheableAspect() {
        CLEANER_SERVICE = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-clean")
                .setDaemon(true)
                .build()
        );
        UPDATER_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-update")
                .setDaemon(true)
                .build()
        );

        this.defaultCacheManager = new CacheManager(this);
        this.cacheManagers = new ConcurrentHashMap<>(0);

        this.CLEANER_SERVICE.scheduleAtFixedRate(() -> {
            this.defaultCacheManager.clean();

            for (CacheManager cacheManager : this.cacheManagers.values()) {
                cacheManager.clean();
            }
        }, 1L, 1L, TimeUnit.SECONDS);

        this.UPDATER_SERVICE.submit(() -> {
            while (true) {
                try {
                    this.defaultCacheManager.update();

                    for (CacheManager cacheManager : this.cacheManagers.values()) {
                        cacheManager.update();
                    }
                } catch (final Throwable ex) {
                    LOGGER.error(ex);
                }
            }
        });

    }

    // TODO: Performance check, is this key reader efficient?

    private static final Pattern KEY_REGEX = Pattern.compile("\\$\\((?<a>[\\w.-]*)\\)");
    private static final Pattern ARGUMENT_REGEX = Pattern.compile("arg(?<arg>[0-9])");

    private String readAnnotationKey(JoinPoint point, String value, boolean ignoreNull) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        Matcher matcher = KEY_REGEX.matcher(value);
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String result = value;

        while (matcher.find()) {
            String group = matcher.group("a");
            String resultField = this.scanArgumentPattern(point, method, group);

            if (resultField != null) {
                result = result.replace("$(" + group + ")", resultField);
            } else if (ignoreNull) {
                result = result.replace("$(" + group + ")", "null");
            } else {
                throw new NullPointerException("The field/parameter " + group + " results null.");
            }
        }

        return result;
    }

    @Nullable
    private String scanArgumentPattern(JoinPoint point, Method method, String fieldString) {
        String[] fields = fieldString.split("\\.");
        Object[] arguments = point.getArgs();

        Object fieldObject = null;
        for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
            String fieldName = fields[fieldIndex];

            if (fieldIndex == 0) {
                Matcher argMatcher = ARGUMENT_REGEX.matcher(fieldName);
                if (argMatcher.find()) {

                    int argumentId = Integer.parseInt(argMatcher.group("arg"));
                    if (arguments.length <= argumentId) {

                        throw new IllegalArgumentException("The argument with id " + argumentId +
                                " does not exists in method " + point.getSignature().getName() +
                                " (max argument is " + (arguments.length - 1) + ")");
                    }

                    fieldObject = arguments[argumentId];
                } else {
                    for (int i = 0; i < method.getParameterCount(); i++) {
                        if (method.getParameters()[i].getName().equals(fieldName)) {
                            fieldObject = arguments[i];
                            break;
                        }
                    }
                }
                continue;
            }

            if (fieldObject == null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < fieldIndex; i++) {
                    stringBuilder.append(fields[i]);

                    if (i + 1 < fieldIndex) {
                        stringBuilder.append(".");
                    }
                }

                throw new NullPointerException("The field " + stringBuilder.toString() + " is null! cannot go through " + stringBuilder.toString() + "." + fields[fieldIndex]);
            }

            try {
                Field field = fieldObject.getClass().getDeclaredField(fieldName);

                AccessUtil.setAccessible(field);
                fieldObject = field.get(fieldObject);
            } catch (ReflectiveOperationException ex) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < fieldIndex; i++) {
                    stringBuilder.append(fields[i]);

                    if (i + 1 < fieldIndex) {
                        stringBuilder.append(".");
                    }
                }

                throw new IllegalArgumentException("The field " + stringBuilder.toString() + "." + fields[fieldIndex] + " doesn't not exists!");
            }

        }

        return fieldObject != null ? fieldObject.toString() : null;
    }

    public CacheManager getCacheManager(Class<?> type) {
        CacheManager cacheManager = this.cacheManagers.getOrDefault(type, null);

        if (cacheManager == null && type.getAnnotation(EnableOwnCacheManager.class) != null) {
            cacheManager = new CacheManager(this);
            this.cacheManagers.put(type, cacheManager);
        }

        return cacheManager == null ? this.defaultCacheManager : cacheManager;
    }

    public CacheKeyAbstract toKey(JoinPoint point, String key) {
        if (key != null && !key.isEmpty()) {
            return new CacheKeyString(point, key);
        }

        return new CacheKeyMethod(point);
    }

    @Around("execution(* *(..)) && @annotation(org.imanity.framework.Cacheable)")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final Cacheable annotation = method.getAnnotation(Cacheable.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.key(), annotation.ignoreKeyNull()));

        return this.getCacheManager(method.getDeclaringClass()).cache(key, point, annotation.unless(), annotation.forever() ? -1 : annotation.lifetime(), annotation.unit(), annotation.asyncUpdate(), false).through();
    }

    @Around("execution(* *(..)) && @annotation(org.imanity.framework.CachePut)")
    public Object cachePut(ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final CachePut annotation = method.getAnnotation(CachePut.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.value(), annotation.ignoreKeyNull()));

        return this.getCacheManager(method.getDeclaringClass()).cache(key, point, annotation.unless(), annotation.forever() ? -1 : annotation.lifetime(), annotation.unit(), annotation.asyncUpdate(), false).through();
    }

    @Before(
           "execution(* *(..)) && @annotation(org.imanity.framework.CacheEvict)"
    )
    public void evict(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        CacheEvict annotation = method.getAnnotation(CacheEvict.class);
        String keyString = this.readAnnotationKey(point, annotation.value(), annotation.ignoreKeyNull());

        this.getCacheManager(method.getDeclaringClass()).evict(point, keyString);
    }

    @Before
            (
                    // @checkstyle StringLiteralsConcatenation (3 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.imanity.framework.Cacheable.ClearBefore)"
            )
    public void preFlush(final JoinPoint point) {
        this.flush(point, "before the call");
    }

    @After
            (
                    // @checkstyle StringLiteralsConcatenation (2 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.imanity.framework.Cacheable.ClearAfter)"
            )
    public void postFlush(final JoinPoint point) {
        this.flush(point, "after the call");
    }

    private void flush(final JoinPoint point, final String when) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        this.getCacheManager(method.getDeclaringClass()).flush(point);
    }

    protected boolean isCreateTunnel(final CacheableAspect.Tunnel tunnel) {
        return tunnel == null || (tunnel.expired() && !tunnel.asyncUpdate());
    }

    @ToString
    protected static final class Tunnel {

        private final ProceedingJoinPoint point;
        private final CacheKeyAbstract key;
        private final boolean async;
        private final long lifetime;
        private final TimeUnit timeUnit;

        private transient boolean executed;
        private transient long expiredTime;
        private transient boolean hasResult;

        private transient SoftReference<Object> cached;

        Tunnel(final ProceedingJoinPoint point, final CacheKeyAbstract key, final boolean async, long lifetime, TimeUnit timeUnit) {
            this.point = point;
            this.key = key;
            this.async = async;
            this.lifetime = lifetime;
            this.timeUnit = timeUnit;

            if (this.lifetime != 0L) {
                final long millis = timeUnit.toMillis(this.lifetime);
                this.expiredTime = System.currentTimeMillis() + millis;
            }
        }

        public Tunnel copy() {
            return new Tunnel(
                    this.point, this.key, this.async, this.lifetime, this.timeUnit
            );
        }

        public synchronized Object through() throws Throwable {
            if (!this.executed) {

                final long start = System.currentTimeMillis();

                final Object result = this.point.proceed();

                this.hasResult = result != null;
                this.cached = new SoftReference<>(result);

                if (this.lifetime == -1) {
                    this.expiredTime = Long.MAX_VALUE;

                } else if (this.lifetime == 0) {
                    this.expiredTime = 0L;

                } else {
                    final long millis = this.timeUnit.toMillis(this.lifetime);
                    this.expiredTime = start + millis;

                }
                this.executed = true;
            }

            return this.cached.get();
        }

        public boolean hasResult() {
            return this.hasResult;
        }

        public boolean expired() {
            final boolean expired = this.expiredTime < System.currentTimeMillis();
            final boolean collected = this.executed
                    && this.hasResult
                    && this.cached.get() == null;
            return this.executed && (expired || collected);
        }

        public boolean asyncUpdate() {
            return this.async;
        }

        SoftReference<Object> cached() {
            return this.cached;
        }
    }
}
