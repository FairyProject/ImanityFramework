package org.imanity.framework;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheEvict {

    @Language("JavaScript") String value();

    @Language("JavaScript") String condition() default "";

    /**
     * Prevent to store value if one of argument were null
     *
     */
    boolean preventArgumentNull() default true;

}
