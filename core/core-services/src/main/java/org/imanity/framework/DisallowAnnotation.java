package org.imanity.framework;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisallowAnnotation {

    Class<? extends Annotation>[] value();

}
