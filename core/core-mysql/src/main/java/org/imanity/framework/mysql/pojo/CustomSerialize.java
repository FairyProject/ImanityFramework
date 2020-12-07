package org.imanity.framework.mysql.pojo;

import org.imanity.framework.ObjectSerializer;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomSerialize {
	Class<? extends ObjectSerializer<?, ?>> value();
}
