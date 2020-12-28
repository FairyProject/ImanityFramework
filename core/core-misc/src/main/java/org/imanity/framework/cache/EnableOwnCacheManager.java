package org.imanity.framework.cache;

import org.imanity.framework.cache.manager.CacheManager;
import org.imanity.framework.cache.manager.CacheManagerConcurrentMap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableOwnCacheManager {

    Class<? extends CacheManager> value() default CacheManagerConcurrentMap.class;

}
