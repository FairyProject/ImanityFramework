/*
 * Copyright (C) Imanity - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Only authorized persons are qualified to use and view the source code
 *  * Proprietary and confidential
 *  * Written by LeeGod <leegod@imanity.dev>, Jan 2021
 */

package org.imanity.framework.bukkit.listener.asm;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import lombok.NonNull;

import java.util.concurrent.ConcurrentMap;

public class SafeClassDefiner implements ClassDefiner {
    /* default */ static final SafeClassDefiner INSTANCE = new SafeClassDefiner();

    private SafeClassDefiner() {}

    private final ConcurrentMap<ClassLoader, GeneratedClassLoader> loaders = new MapMaker().weakKeys().makeMap();

    @NonNull
    @Override
    public Class<?> defineClass(@NonNull ClassLoader parentLoader, @NonNull String name, @NonNull byte[] data) {
        GeneratedClassLoader loader = loaders.computeIfAbsent(parentLoader, GeneratedClassLoader::new);
        synchronized (loader.getClassLoadingLock(name)) {
            Preconditions.checkState(!loader.hasClass(name), "%s already defined", name);
            Class<?> c = loader.define(name, data);
            assert c.getName().equals(name);
            return c;
        }
    }

    private static class GeneratedClassLoader extends ClassLoader {
        static {
            ClassLoader.registerAsParallelCapable();
        }

        protected GeneratedClassLoader(@NonNull ClassLoader parent) {
            super(parent);
        }

        private Class<?> define(@NonNull String name, byte[] data) {
            synchronized (getClassLoadingLock(name)) {
                assert !hasClass(name);
                Class<?> c = defineClass(name, data, 0, data.length);
                resolveClass(c);
                return c;
            }
        }

        @Override
        @NonNull
        public Object getClassLoadingLock(@NonNull String name) {
            return super.getClassLoadingLock(name);
        }

        public boolean hasClass(@NonNull String name) {
            synchronized (getClassLoadingLock(name)) {
                try {
                    Class.forName(name);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }
    }
}
