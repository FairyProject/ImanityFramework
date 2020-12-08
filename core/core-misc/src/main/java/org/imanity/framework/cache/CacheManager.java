package org.imanity.framework.cache;

import org.aspectj.lang.JoinPoint;
import org.imanity.framework.cache.impl.CacheKeyAbstract;

import java.util.concurrent.*;

public class CacheManager {

    private transient final ConcurrentMap<CacheKeyAbstract, CacheWrapper<?>> cache;

    private final CacheableAspect cacheableAspect;

    public CacheManager(CacheableAspect cacheableAspect) {
        this.cacheableAspect = cacheableAspect;

        this.cache = new ConcurrentHashMap<>();
    }

    public void clean() {
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public CacheWrapper<?> find(CacheKeyAbstract key) {
        CacheWrapper<?> wrapper = this.cache.get(key);
        if (wrapper != null && wrapper.isExpired()) {
            this.cache.remove(key);
            return null;
        }

        return wrapper;
    }

    public void cache(CacheKeyAbstract key, CacheWrapper<?> wrapper) throws Throwable {
        this.cache.put(key, wrapper);
    }

    public void evict(JoinPoint point, String keyString) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (!key.equals(this.cacheableAspect.toKey(point, keyString))) {
                continue;
            }
            this.cache.remove(key);
        }
    }

    public void flush(JoinPoint point) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (!key.sameTarget(point, null)) {
                continue;
            }
            this.cache.remove(key);
        }
    }

}
