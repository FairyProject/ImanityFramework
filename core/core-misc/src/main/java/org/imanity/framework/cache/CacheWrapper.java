/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
