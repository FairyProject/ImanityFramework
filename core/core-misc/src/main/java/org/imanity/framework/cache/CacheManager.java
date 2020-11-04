package org.imanity.framework.cache;

import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.imanity.framework.Cacheable;
import org.imanity.framework.cache.impl.CacheKeyAbstract;

import java.lang.reflect.Method;
import java.util.concurrent.*;

public class CacheManager {

    private static final ConcurrentMap<Class<?>, Unless> UNLESS_CACHE = new ConcurrentHashMap<>();

    private transient final ConcurrentMap<CacheKeyAbstract, CacheableAspect.Tunnel> tunnels;
    private transient final BlockingQueue<CacheKeyAbstract> updatekeys;

    private final CacheableAspect cacheableAspect;

    public CacheManager(CacheableAspect cacheableAspect) {
        this.cacheableAspect = cacheableAspect;

        this.tunnels = new ConcurrentHashMap<>(0);
        this.updatekeys = new LinkedBlockingQueue<>();
    }

    public void clean() {
        synchronized (this.tunnels) {
            for (final CacheKeyAbstract key : this.tunnels.keySet()) {
                if (this.tunnels.get(key).expired()
                        && !this.tunnels.get(key).asyncUpdate()) {
                    this.tunnels.remove(key);
                }
            }
        }
    }

    public void update() throws Throwable {
        final CacheKeyAbstract key = this.updatekeys.take();
        final CacheableAspect.Tunnel tunnel = this.tunnels.get(key);
        if (tunnel != null && tunnel.expired()) {
            final CacheableAspect.Tunnel newTunnel = tunnel.copy();
            newTunnel.through();
            this.tunnels.put(key, newTunnel);
        }
    }

    public CacheableAspect.Tunnel cache(CacheKeyAbstract key,
                                        Method method,
                                        ProceedingJoinPoint point,
                                        Class<? extends Unless>[] unlesses,
                                        boolean asyncPut,
                                        boolean forcePut) throws Throwable {
        synchronized (this.tunnels) {

            CacheableAspect.Tunnel tunnel;
            if (forcePut) {
                tunnel = null;
            } else {
                tunnel = this.tunnels.get(key);
            }

            boolean put = true;

            if (forcePut || this.cacheableAspect.isCreateTunnel(tunnel)) {
                tunnel = new CacheableAspect.Tunnel(
                        point, key, asyncPut
                );

                if (!this.checkUnless(unlesses, tunnel)) {
                    this.tunnels.put(key, tunnel);
                } else {
                    put = false;
                }
            }

            if (put && tunnel.expired() && tunnel.asyncUpdate()) {
                this.updatekeys.offer(key);
            }

            return tunnel;
        }
    }

    @SneakyThrows
    private boolean checkUnless(Class<? extends Unless>[] unlesses, CacheableAspect.Tunnel tunnel) {
        for (Class<? extends Unless> unlessClass : unlesses) {
            Unless unless;

            if (UNLESS_CACHE.containsKey(unlessClass)) {
                unless = UNLESS_CACHE.get(unlessClass);
            } else {
                unless = unlessClass.newInstance();
            }

            if (unless.unless(tunnel)) {
                return true;
            }
        }

        return false;
    }

    public void evict(JoinPoint point, String keyString) {
        synchronized (this.tunnels) {
            for (final CacheKeyAbstract key : this.tunnels.keySet()) {
                if (!key.equals(this.cacheableAspect.toKey(point, keyString))) {
                    continue;
                }
                this.tunnels.remove(key);
            }
        }
    }

    public void flush(JoinPoint point) {
        synchronized (this.tunnels) {
            for (final CacheKeyAbstract key : this.tunnels.keySet()) {
                if (!key.sameTarget(point, null)) {
                    continue;
                }
                this.tunnels.remove(key);
            }
        }
    }

}
