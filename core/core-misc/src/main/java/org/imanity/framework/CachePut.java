package org.imanity.framework;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachePut {

    @Language("JavaScript") String value();

    @Language("JavaScript") String condition() default "";

    boolean asyncUpdate() default false;

    int lifetime() default 1;

    TimeUnit unit() default TimeUnit.MINUTES;

    boolean forever() default false;

    /**
     * Prevent to store value if one of argument were null
     *
     */
    boolean preventArgumentNull() default true;

}
