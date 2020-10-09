package org.imanity.framework.bukkit.listener;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilteredEventHandler {

    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreCancelled() default false;

    boolean ignoreFilters() default false;

}
