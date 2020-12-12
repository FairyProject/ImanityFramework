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

package org.imanity.framework;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.command.ICommandExecutor;
import org.imanity.framework.config.CoreConfig;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.exception.OptionNotEnabledException;
import org.imanity.framework.factory.ClassFactory;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.LibraryHandler;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.locale.LocaleRepository;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.task.ITaskScheduler;
import org.imanity.framework.util.Terminable;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImanityCommon {

    private static final Set<Library> GLOBAL_LIBRARIES = ImmutableSet.of(
            // SQL
            Library.MARIADB_DRIVER,
            Library.HIKARI,
            Library.H2_DRIVER,
            Library.MYSQL_DRIVER,
            Library.POSTGRESQL_DRIVER,

            // MONGO
            Library.MONGO_DB_SYNC,
            Library.MONGO_DB_CORE,


            Library.BSON,
            Library.CAFFEINE
    );
    public static final String METADATA_PREFIX = "Imanity_";

    public static ImanityPlatform PLATFORM;
    public static CoreConfig CORE_CONFIG;
    public static ServiceHandler SERVICE_HANDLER;

    @Autowired
    public static LocaleHandler LOCALE_HANDLER;

    @Autowired
    public static LocaleRepository LOCALE_REPOSITORY;

    public static LibraryHandler LIBRARY_HANDLER;

    public static ICommandExecutor COMMAND_EXECUTOR;
    public static IEventHandler EVENT_HANDLER;
    public static ITaskScheduler TASK_SCHEDULER;
    public static ObjectMapper JACKSON_MAPPER;

    @Autowired
    public static ServerHandler SERVER_HANDLER;

    private static final List<Terminable> TERMINATES = new ArrayList<>();

    private static boolean LIBRARIES_INITIALIZED, BRIDGE_INITIALIZED;

    public static void init() {
        ImanityCommon.loadLibraries();

        ImanityCommon.CORE_CONFIG = new CoreConfig();
        ImanityCommon.CORE_CONFIG.loadAndSave();

        ClassFactory.loadClasses();

        ImanityCommon.PLATFORM.preServiceLoaded();

        ImanityCommon.SERVICE_HANDLER = new ServiceHandler();
        ImanityCommon.SERVICE_HANDLER.registerServices();
        ImanityCommon.SERVICE_HANDLER.init();
    }

    public static void loadLibraries() {

        if (ImanityCommon.LIBRARIES_INITIALIZED) {
            return;
        }
        ImanityCommon.LIBRARIES_INITIALIZED = true;

        getLogger().info("Loading Libraries");

        ImanityCommon.LIBRARY_HANDLER = new LibraryHandler();
        ImanityCommon.LIBRARY_HANDLER.downloadLibraries(GLOBAL_LIBRARIES);
        ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(GLOBAL_LIBRARIES);

        try {
            Class.forName("com.google.common.collect.ImmutableList");
        } catch (ClassNotFoundException ex) {
            // Below 1.8
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(Library.GUAVA);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(Library.GUAVA);
        }

        try {
            Class.forName("it.unimi.dsi.fastutil.Arrays");
        } catch (ClassNotFoundException ex) {
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(Library.FAST_UTIL);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(Library.FAST_UTIL);
        }

        try {
            Class.forName("org.yaml");
        } catch (ClassNotFoundException ex) {
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(Library.YAML);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(Library.YAML);
        }

        Library redisson;
        try {
            Class.forName("io.netty.channel.Channel");

            redisson = Library.REDISSON_RELOCATED;
        } catch (ClassNotFoundException ex) {
            redisson = Library.REDISSON;
        }

        ImanityCommon.LIBRARY_HANDLER.downloadLibraries(redisson);
        ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(redisson);

        FrameworkMisc.LIBRARY_HANDLER = ImanityCommon.LIBRARY_HANDLER;
    }

    public static Logger getLogger() {
        return ImanityCommon.PLATFORM.getLogger();
    }

    public static <T> T getService(Class<T> type) {
        return (T) SERVICE_HANDLER.getServiceInstance(type);
    }

    public static void registerAutowired(Object instance) {
        SERVICE_HANDLER.registerAutowired(instance);
    }

    public static void shutdown() throws Throwable {
        if (ImanityCommon.CORE_CONFIG.USE_REDIS) {
            SERVER_HANDLER.changeServerState(ServerState.STOPPING);
        }

        synchronized (ImanityCommon.TERMINATES) {
            for (Terminable terminable : ImanityCommon.TERMINATES) {
                terminable.close();
            }
        }

        ImanityCommon.SERVICE_HANDLER.stop();
        FrameworkMisc.close();
    }

    public static String translate(UUID uuid, String key) {
        if (!ImanityCommon.CORE_CONFIG.USE_LOCALE) {
            throw new OptionNotEnabledException("use_locale", "org.imanity.framework.config.yml");
        }
        LocaleData localeData = LOCALE_REPOSITORY.find(uuid);
        Locale locale;
        if (localeData == null || localeData.getLocale() == null) {
            locale = ImanityCommon.LOCALE_HANDLER.getDefaultLocale();
        } else {
            locale = localeData.getLocale();
        }
        return locale.get(key);
    }

    public static void addTerminable(Terminable terminable) {
        synchronized (ImanityCommon.TERMINATES) {
            ImanityCommon.TERMINATES.add(terminable);
        }
    }

    public static Builder builder() {
        if (ImanityCommon.BRIDGE_INITIALIZED) {
            throw new IllegalStateException("Already build!");
        }

        ImanityCommon.BRIDGE_INITIALIZED = true;
        return new Builder();
    }

    public static class Builder {

        private ImanityPlatform platform;
        private ICommandExecutor commandExecutor;
        private IEventHandler eventHandler;
        private ITaskScheduler taskScheduler;
        private ObjectMapper mapper;

        public Builder platform(ImanityPlatform bridge) {
            this.platform = bridge;
            return this;
        }

        public Builder commandExecutor(ICommandExecutor commandExecutor) {
            this.commandExecutor = commandExecutor;
            return this;
        }

        public Builder eventHandler(IEventHandler eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        public Builder taskScheduler(ITaskScheduler taskScheduler) {
            this.taskScheduler = taskScheduler;
            return this;
        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public void init() {
            if (this.platform != null) {
                ImanityCommon.PLATFORM = this.platform;
                FrameworkMisc.PLATFORM = this.platform;
            }
            if (this.commandExecutor != null) {
                ImanityCommon.COMMAND_EXECUTOR = this.commandExecutor;
            }
            if (this.eventHandler != null) {
                ImanityCommon.EVENT_HANDLER = this.eventHandler;
                FrameworkMisc.EVENT_HANDLER = this.eventHandler;
            }
            if (this.taskScheduler != null) {
                ImanityCommon.TASK_SCHEDULER = this.taskScheduler;
                FrameworkMisc.TASK_SCHEDULER = this.taskScheduler;
            }
            if (this.mapper == null) {
                ImanityCommon.JACKSON_MAPPER = new ObjectMapper();
                ImanityCommon.JACKSON_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
                ImanityCommon.JACKSON_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            } else {
                ImanityCommon.JACKSON_MAPPER = this.mapper;
            }

            FrameworkMisc.JACKSON_MAPPER = ImanityCommon.JACKSON_MAPPER;

            ImanityCommon.init();
        }

    }

}
