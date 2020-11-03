package org.imanity.framework.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.imanity.framework.Cacheable;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.*;

public class CacheManager {

    private transient final ConcurrentMap<CacheableAspect.Key, CacheableAspect.Tunnel> tunnels;
    private transient final BlockingQueue<CacheableAspect.Key> updatekeys;

    private final CacheableAspect cacheableAspect;

    public CacheManager(CacheableAspect cacheableAspect) {
        this.cacheableAspect = cacheableAspect;

        this.tunnels = new ConcurrentHashMap<>(0);
        this.updatekeys = new LinkedBlockingQueue<>();
    }

    public void clean() {
        synchronized (this.tunnels) {
            for (final CacheableAspect.Key key : this.tunnels.keySet()) {
                if (this.tunnels.get(key).expired()
                        && !this.tunnels.get(key).asyncUpdate()) {
                    this.tunnels.remove(key);
                }
            }
        }
    }

    public void update() throws Throwable {
        final CacheableAspect.Key key = this.updatekeys.take();
        final CacheableAspect.Tunnel tunnel = this.tunnels.get(key);
        if (tunnel != null && tunnel.expired()) {
            final CacheableAspect.Tunnel newTunnel = tunnel.copy();
            newTunnel.through();
            this.tunnels.put(key, newTunnel);
        }
    }

    public CacheableAspect.Tunnel cache(CacheableAspect.Key key, Cacheable annotation, Method method, ProceedingJoinPoint point) throws Throwable {
        synchronized (this.tunnels) {

            for (final Class<?> before : annotation.before()) {
                final boolean flag = (boolean) before.getMethod("flushBefore").invoke(method.getClass());
                if (flag) {
                    this.cacheableAspect.preFlush(point);
                }
            }

            CacheableAspect.Tunnel tunnel = this.tunnels.get(key);
            if (this.cacheableAspect.isCreateTunnel(tunnel)) {
                tunnel = new CacheableAspect.Tunnel(
                        point, key, annotation.asyncUpdate()
                );
                this.tunnels.put(key, tunnel);
            }
            if (tunnel.expired() && tunnel.asyncUpdate()) {
                this.updatekeys.offer(key);
            }
            for (final Class<?> after : annotation.after()) {
                final boolean flag = (boolean) after.getMethod("flushAfter").invoke(method.getClass());
                if (flag) {
                    this.cacheableAspect.postFlush(point);
                }
            }

            return tunnel;
        }
    }

    public void evict(JoinPoint point, String keyString) {
        synchronized (this.tunnels) {
            for (final CacheableAspect.Key key : this.tunnels.keySet()) {
                if (!key.sameTarget(point)) {
                    continue;
                }

                if (!Objects.equals(key.key, keyString)) {
                    continue;
                }
                this.tunnels.remove(key);
            }
        }
    }

    public void flush(JoinPoint point) {
        synchronized (this.tunnels) {
            for (final CacheableAspect.Key key : this.tunnels.keySet()) {
                if (!key.sameTarget(point)) {
                    continue;
                }
                this.tunnels.remove(key);
            }
        }
    }

}
