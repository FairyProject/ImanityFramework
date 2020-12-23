package org.imanity.framework.command.annotation;

import org.imanity.framework.command.PresenceProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CommandPresence {

    Class<? extends PresenceProvider> value();

}
