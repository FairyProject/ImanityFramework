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
import org.imanity.framework.Cacheable;
import org.imanity.framework.util.AccessUtil;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    // TODO: shut it down when closing
    private transient final ScheduledExecutorService cleanerService;
    private transient final ExecutorService updaterService;

    public CacheableAspect() {
        this.cleanerService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-clean")
                .setDaemon(true)
                .build()
        );
        this.updaterService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("cacheable-update")
                .setDaemon(true)
                .build()
        );

        this.defaultCacheManager = new CacheManager(this);
        this.cacheManagers = new ConcurrentHashMap<>(0);

        this.cleanerService.scheduleAtFixedRate(() -> {
            this.defaultCacheManager.clean();

            for (CacheManager cacheManager : this.cacheManagers.values()) {
                cacheManager.clean();
            }
        }, 1L, 1L, TimeUnit.SECONDS);

        this.updaterService.submit(() -> {
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

                    if (i + 1 <= fieldIndex) {
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

                    if (i + 1 <= fieldIndex) {
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

        return cacheManager;
    }

    @Around("execution(* *(..)) && @annotation(org.imanity.framework.Cacheable)")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final Cacheable annotation = method.getAnnotation(Cacheable.class);
        final CacheableAspect.Key key = new CacheableAspect.Key(point, readAnnotationKey(point, annotation.key(), annotation.ignoredNull()));

        return this.getCacheManager(method.getDeclaringClass()).cache(key, annotation, method, point).through();
    }

    @Before(
           "execution(* *(..)) && @annotation(org.imanity.framework.CacheEvict)"
    )
    public void evict(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        CacheEvict annotation = method.getAnnotation(CacheEvict.class);
        String keyString = this.readAnnotationKey(point, annotation.value(), annotation.ignoredNull());

        this.getCacheManager(method.getDeclaringClass()).evict(point, keyString);
    }

    @Before
            (
                    // @checkstyle StringLiteralsConcatenation (3 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.imanity.framework.Cacheable.FlushBefore)"
            )
    public void preFlush(final JoinPoint point) {
        this.flush(point, "before the call");
    }

    @After
            (
                    // @checkstyle StringLiteralsConcatenation (2 lines)
                    "execution(* *(..))"
                            + " && @annotation(org.imanity.framework.Cacheable.FlushAfter)"
            )
    public void postFlush(final JoinPoint point) {
        this.flush(point, "after the call");
    }

    private void flush(final JoinPoint point, final String when) {
        this.getCacheManager(point.getThis().getClass()).flush(point);
    }

    protected boolean isCreateTunnel(final CacheableAspect.Tunnel tunnel) {
        return tunnel == null || (tunnel.expired() && !tunnel.asyncUpdate());
    }

    @ToString
    protected static final class Tunnel {

        private final transient ProceedingJoinPoint point;
        private final transient CacheableAspect.Key key;
        private final transient boolean async;
        private transient boolean executed;
        private transient long lifetime;
        private transient boolean hasResult;

        private transient SoftReference<Object> cached;

        Tunnel(final ProceedingJoinPoint pnt,
               final CacheableAspect.Key akey, final boolean asy) {
            this.point = pnt;
            this.key = akey;
            this.async = asy;
        }

        public Tunnel copy() {
            return new Tunnel(
                    this.point, this.key, this.async
            );
        }

        public synchronized Object through() throws Throwable {
            if (!this.executed) {

                final long start = System.currentTimeMillis();

                final Object result = this.point.proceed();

                this.hasResult = result != null;
                this.cached = new SoftReference<>(result);

                final Method method = MethodSignature.class
                        .cast(this.point.getSignature())
                        .getMethod();

                final Cacheable annotation = method.getAnnotation(Cacheable.class);

                if (annotation.forever()) {
                    this.lifetime = Long.MAX_VALUE;

                } else if (annotation.lifetime() == 0) {
                    this.lifetime = 0L;

                } else {
                    final long millis = annotation.unit().toMillis(annotation.lifetime());
                    this.lifetime = start + millis;

                }
                this.executed = true;
            }

            return this.key.through(this.cached.get());
        }

        public boolean expired() {
            final boolean expired = this.lifetime < System.currentTimeMillis();
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

    @ToString
    protected static class Key {

        private final transient long start;
        private final transient AtomicInteger accessed;
        private final transient Method method;
        private final transient Object target;
        private final transient Object[] arguments;

        @Nullable
        protected final transient String key;

        Key(final JoinPoint point, String key) {
            this.start = System.currentTimeMillis();
            this.accessed = new AtomicInteger();
            this.method = ((MethodSignature) point.getSignature()).getMethod();
            this.target = CacheableAspect.Key.findTarget(point);
            this.arguments = point.getArgs();
            this.key = key;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(this.method, this.key);
        }

        @Override
        public final boolean equals(final Object obj) {
            final boolean equals;
            if (this == obj) {
                equals = true;
            } else if (obj instanceof CacheableAspect.Key) {
                final CacheableAspect.Key key = CacheableAspect.Key.class.cast(obj);
                equals = key.method.equals(this.method)
                        && this.target.equals(key.target)
                        && Arrays.deepEquals(key.arguments, this.arguments)
                        && Objects.equals(key.key, this.key);
            } else {
                equals = false;
            }
            return equals;
        }

        public Object through(final Object result) {
            return result;
        }

        public final boolean sameTarget(final JoinPoint point) {
            return CacheableAspect.Key.findTarget(point).equals(this.target);
        }

        private static Object findTarget(final JoinPoint point) {
            final Object tgt;
            final Method method = MethodSignature.class
                    .cast(point.getSignature()).getMethod();
            if (Modifier.isStatic(method.getModifiers())) {
                tgt = method.getDeclaringClass();
            } else {
                tgt = point.getTarget();
            }
            return tgt;
        }

    }
}
