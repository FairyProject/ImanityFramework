package org.imanity.framework.data.annotation;

import org.imanity.framework.data.type.DataConverterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StoreDataElement {

    String name() default "";

}
