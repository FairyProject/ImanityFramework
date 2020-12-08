package org.imanity.framework.cache;

import lombok.Data;

import java.io.Serializable;
import java.lang.ref.SoftReference;

@Data
public class CacheWrapper<T> implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private SoftReference<T> cacheObject;

    private long lastLoadTime;

    private long expireTime;

    public CacheWrapper() {
    }

    public CacheWrapper(T cacheObject, long expireTime) {
        this.cacheObject = new SoftReference<>(cacheObject);
        this.lastLoadTime = System.currentTimeMillis();
        this.expireTime = expireTime;
    }

    public CacheWrapper(T cacheObject, long expireTime, long lastLoadTime) {
        this.cacheObject = new SoftReference<>(cacheObject);
        this.lastLoadTime = lastLoadTime;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        if (expireTime > 0) {
            return (System.currentTimeMillis() - lastLoadTime) > expireTime;
        }
        return false;
    }

    public Object get() {
        return this.cacheObject.get();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        @SuppressWarnings("unchecked")
        CacheWrapper<T> tmp = (CacheWrapper<T>) super.clone();
        tmp.setCacheObject(this.cacheObject);
        return tmp;
    }

}
