package org.imanity.framework.bungee.plugin.annotation;

import net.md_5.bungee.api.plugin.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginProvider {

    Class<? extends Plugin> value();

}
