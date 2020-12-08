package org.imanity.framework.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.SneakyThrows;
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
import org.imanity.framework.cache.script.AbstractScriptParser;
import org.imanity.framework.cache.script.JavaScriptParser;
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

    private final AbstractScriptParser scriptParser;

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
        this.scriptParser = new JavaScriptParser();

        CLEANER_SERVICE.scheduleAtFixedRate(() -> {
            this.defaultCacheManager.clean();

            for (CacheManager cacheManager : this.cacheManagers.values()) {
                cacheManager.clean();
            }
        }, 1L, 1L, TimeUnit.SECONDS);

    }

    // TODO: Performance check, is this key reader efficient?

    private String readAnnotationKey(JoinPoint point, String value, boolean preventNull) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        Object[] args = point.getArgs();

        if (preventNull) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    throw new IllegalArgumentException("The argument with index " + i + " in method " + point.getSignature().getName() + " is null!");
                }
            }
        }

        try {
            return this.scriptParser.getDefinedCacheKey(value, point.getTarget(), point.getArgs(), null, false);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return "";
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

    @SneakyThrows
    public boolean checkCondition(String condition, Object target, Object[] arguments, Object retVal, boolean hasRetVal) {
        boolean result = true;
        if (arguments != null && arguments.length > 0 && condition != null && condition.length() > 0) {
            result = this.scriptParser.getElValue(condition, target, arguments, retVal, true, Boolean.class);
        }
        return result;
    }

    @Around("execution(* *(..)) && @annotation(org.imanity.framework.Cacheable)")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final Cacheable annotation = method.getAnnotation(Cacheable.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.key(), annotation.preventArgumentNull()));
        String condition = annotation.condition();

        CacheManager manager = this.getCacheManager(method.getDeclaringClass());
        CacheWrapper<?> wrapper = manager.find(key);

        if (wrapper != null) {
            return wrapper.get();
        }

        Object result = point.proceed();

        if (condition.length() != 0 && !this.checkCondition(condition, point.getTarget(), point.getArgs(), result, true)) {
            return result;
        }

        wrapper = new CacheWrapper<>(result, annotation.forever() ? 0L : annotation.unit().toMillis(annotation.lifetime()));
        manager.cache(key, wrapper);
        return result;
    }

    @Around("execution(* *(..)) && @annotation(org.imanity.framework.CachePut)")
    public Object cachePut(ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final CachePut annotation = method.getAnnotation(CachePut.class);
        CacheKeyAbstract key = this.toKey(point, readAnnotationKey(point, annotation.value(), annotation.preventArgumentNull()));
        String condition = annotation.condition();

        CacheManager manager = this.getCacheManager(method.getDeclaringClass());

        Object result = point.proceed();

        if (condition.length() != 0 && !this.checkCondition(condition, point.getTarget(), point.getArgs(), result, true)) {
            return result;
        }

        CacheWrapper<?> wrapper = new CacheWrapper<>(result, annotation.forever() ? 0L : annotation.unit().toMillis(annotation.lifetime()));
        manager.cache(key, wrapper);
        return result;
    }

    @Before(
           "execution(* *(..)) && @annotation(org.imanity.framework.CacheEvict)"
    )
    public void evict(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        CacheEvict annotation = method.getAnnotation(CacheEvict.class);
        String keyString = this.readAnnotationKey(point, annotation.value(), annotation.preventArgumentNull());
        String condition = annotation.condition();

        if (condition.length() > 0) {
            boolean conditionResult = this.checkCondition(condition, point.getTarget(), point.getArgs(), null, false);

            if (!conditionResult) {
                return;
            }
        }
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

}
