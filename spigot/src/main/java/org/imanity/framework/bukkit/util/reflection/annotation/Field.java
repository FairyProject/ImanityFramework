package org.imanity.framework.bukkit.util.reflection.annotation;

import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves the annotated {@link org.imanity.framework.bukkit.util.reflection.resolver.wrapper.FieldWrapper} or {@link java.lang.reflect.Field} field to the first matching field name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

	/**
	 * Name of the class to load this field from
	 *
	 * @return name of the class
	 */
	String className();

	/**
	 * Possible names of the field. Use <code>&gt;</code> or <code>&lt;</code> as a name prefix in combination with {@link #versions()} to specify versions newer- or older-than.
	 *
	 * @return names of the field
	 */
	String[] value();

	/**
	 * Specific versions for the names.
	 *
	 * @return Array of versions for the class names
	 */
	MinecraftReflection.Version[] versions() default {};

	/**
	 * Whether to ignore any reflection exceptions thrown. Defaults to <code>true</code>
	 *
	 * @return whether to ignore exceptions
	 */
	boolean ignoreExceptions() default true;
}
