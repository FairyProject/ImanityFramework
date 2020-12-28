package org.imanity.framework.cache.manager;

import com.google.common.collect.ImmutableMap;
import org.aspectj.lang.JoinPoint;
import org.imanity.framework.cache.CacheWrapper;
import org.imanity.framework.cache.CacheableAspect;
import org.imanity.framework.cache.impl.CacheKeyAbstract;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class CacheManagerConcurrentMap implements CacheManager {

    private transient ConcurrentMap<CacheKeyAbstract, CacheWrapper<?>> cache;

    private CacheableAspect cacheableAspect;

    @Override
    public void init(CacheableAspect cacheableAspect) {
        this.cacheableAspect = cacheableAspect;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void clean() {
        this.cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Override
    public Map<CacheKeyAbstract, CacheWrapper<?>> getAsMap() {
        return ImmutableMap.copyOf(this.cache);
    }

    @Override
    public <T> Collection<T> findByType(Class<T> type) {
        Set<T> results = new HashSet<>();
        for (CacheWrapper<?> wrapper : this.cache.values()) {
            Object object = wrapper.get();
            if (type.isInstance(object)) {
                results.add((T) object);
            }
        }

        return results;
    }

    @Override
    public CacheWrapper<?> find(CacheKeyAbstract key) {
        CacheWrapper<?> wrapper = this.cache.get(key);
        if (wrapper != null && wrapper.isExpired()) {
            this.cache.remove(key);
            return null;
        }

        return wrapper;
    }

    @Override
    public void cache(CacheKeyAbstract key, CacheWrapper<?> wrapper) throws Throwable {
        this.cache.put(key, wrapper);
    }

    @Override
    public void evict(JoinPoint point, String keyString) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (!key.equals(this.cacheableAspect.toKey(point, keyString))) {
                continue;
            }
            this.cache.remove(key);
        }
    }

    @Override
    public void flush(JoinPoint point) {
        for (final CacheKeyAbstract key : this.cache.keySet()) {
            if (!key.sameTarget(point, null)) {
                continue;
            }
            this.cache.remove(key);
        }
    }

}
