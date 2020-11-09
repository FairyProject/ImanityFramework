package org.imanity.framework.aspect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public final class AsyncAspect {

    public static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("imanity-async-%d")
                    .build()
            );

    @Around("execution(@org.imanity.framework.Async * * (..))")
    public Object wrap(final ProceedingJoinPoint point) {
        final Class<?> returned = MethodSignature.class
                .cast(point.getSignature()).getMethod().getReturnType();

        if (!Future.class.isAssignableFrom(returned) && !returned.equals(Void.TYPE)) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Return type is %s, not void or Future, cannot use @Async",
                            point.toShortString(),
                            returned.getCanonicalName()
                    )
            );
        }

        final Future<?> future = EXECUTOR.submit(() -> {
                    Object returned1 = null;
                    try {
                        final Object result1 = point.proceed();
                        if (result1 instanceof Future) {
                            returned1 = ((Future<?>) result1).get();
                        }
                    } catch (final Throwable ex) {
                        throw new IllegalStateException(
                                String.format("%s: Exception thrown", point.toShortString()),
                                ex
                        );
                    }
                    return returned1;
                }
        );
        Object resultObject = null;
        if (Future.class.isAssignableFrom(returned)) {
            resultObject = future;
        }
        return resultObject;
    }

}
