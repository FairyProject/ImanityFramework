package org.imanity.framework.mysql;

import java.lang.annotation.*;

/**
 * Specify the order of the columns. Is used in the create table sql.
 * ColumnOrder({"name","address", ...})
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnOrder {
	String[] value();
}

