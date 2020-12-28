package org.imanity.framework.cache.manager;

import org.aspectj.lang.JoinPoint;
import org.imanity.framework.cache.CacheWrapper;
import org.imanity.framework.cache.CacheableAspect;
import org.imanity.framework.cache.impl.CacheKeyAbstract;

import java.util.Collection;
import java.util.Map;

public interface CacheManager {
    void init(CacheableAspect cacheableAspect);

    void clean();

    Map<CacheKeyAbstract, CacheWrapper<?>> getAsMap();

    <T> Collection<T> findByType(Class<T> type);

    CacheWrapper<?> find(CacheKeyAbstract key);

    void cache(CacheKeyAbstract key, CacheWrapper<?> wrapper) throws Throwable;

    void evict(JoinPoint point, String keyString);

    void flush(JoinPoint point);
}
