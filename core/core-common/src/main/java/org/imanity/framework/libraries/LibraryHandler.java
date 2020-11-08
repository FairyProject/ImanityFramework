/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.libraries;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.libraries.classloader.IsolatedClassLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryHandler {

    private final Path libFolder;

    private final Map<Library, Path> loaded = new ConcurrentHashMap<>();
    private final Map<ImmutableSet<Library>, IsolatedClassLoader> loaders = new HashMap<>();

    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Library Downloader - %d")
        .build());

    public LibraryHandler() {
        File file = new File(ImanityCommon.BRIDGE.getDataFolder(), "libs");
        if (!file.exists()) {
            file.mkdirs();
        }
        this.libFolder = file.toPath();
    }

    public IsolatedClassLoader obtainClassLoaderWith(Collection<Library> libraries) {
        return this.obtainClassLoaderWith(libraries.toArray(new Library[0]));
    }

    public IsolatedClassLoader obtainClassLoaderWith(Library... libraries) {
        ImmutableSet<Library> set = ImmutableSet.copyOf(libraries);

        for (Library dependency : libraries) {
            if (!this.loaded.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        synchronized (this.loaders) {
            IsolatedClassLoader classLoader = this.loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }

            URL[] urls = set.stream()
                    .map(this.loaded::get)
                    .map(file -> {
                        try {
                            return file.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);

            classLoader = new IsolatedClassLoader(urls);
            this.loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void downloadLibraries(Collection<Library> libraries) {
        this.downloadLibraries(libraries.toArray(new Library[0]));
    }

    public void downloadLibraries(Library... libraries) {

        CountDownLatch latch = new CountDownLatch(libraries.length);

        for (Library library : libraries) {
            EXECUTOR.execute(() -> {
                try {
                    loadLibrary(library);
                    ImanityCommon.getLogger().info("Loaded Library " + library.name() + " v" + library.getVersion());
                } catch (Throwable throwable) {
                    ImanityCommon.getLogger().warn("Unable to load library " + library.getFileName() + ".", throwable);
                } finally {
                    latch.countDown();
                }
            });

        };

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

    }

    private void loadLibrary(Library library) throws Exception {
        if (loaded.containsKey(library)) {
            return;
        }

        Path file = this.downloadLibrary(library);

        this.loaded.put(library, file);

        ImanityCommon.BRIDGE.getClassLoader().addJarToClasspath(file);
    }

    protected Path downloadLibrary(Library library) throws LibraryDownloadException {
        Path file = this.libFolder.resolve(library.getFileName() + ".jar");

        if (Files.exists(file)) {
            return file;
        }

        LibraryDownloadException lastError = null;

        for (LibraryRepository repository : LibraryRepository.values()) {
            try {
                repository.download(library, file);
                return file;
            } catch (LibraryDownloadException ex) {
                lastError = ex;
            }
        }

        throw Objects.requireNonNull(lastError);
    }
}
