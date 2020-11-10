package org.imanity.framework.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.ScheduledAtFixedRate;

import java.lang.reflect.Method;

@Aspect
public class ScheduleAspect {

    @Around("execution(@org.imanity.framework.ScheduledAtFixedRate * * (..))")
    public Object schedule(ProceedingJoinPoint point) {
        final Class<?> returned = ((MethodSignature) point.getSignature()).getMethod().getReturnType();

        if (!returned.equals(Void.TYPE)) {
            throw new IllegalStateException(
                    String.format(
                            "%s: Return type is %s, not void, cannot use @ScheduledAtFixedRate",
                            point.toShortString(),
                            returned.getCanonicalName()
                    )
            );
        }

        final Method method = ((MethodSignature) point.getSignature()).getMethod();

        final ScheduledAtFixedRate annotation = method.getAnnotation(ScheduledAtFixedRate.class);
        Runnable runnable = () -> {
            try {
                point.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };

        if (annotation.async()) {
            FrameworkMisc.TASK_SCHEDULER.runAsyncRepeated(runnable, annotation.delay(), annotation.ticks());
        } else {
            FrameworkMisc.TASK_SCHEDULER.runRepeated(runnable, annotation.delay(), annotation.ticks());
        }

        return null;
    }

}
