package org.imanity.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheEvict {

    String value();

    String condition() default "";

    /**
     * Prevent to store value if one of argument were null
     *
     */
    boolean preventArgumentNull() default true;

}
