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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.command.ICommandExecutor;
import org.imanity.framework.config.CoreConfig;
import org.imanity.framework.database.MySQL;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.exception.OptionNotEnabledException;
import org.imanity.framework.factory.ClassFactory;
import org.imanity.framework.factory.FieldFactory;
import org.imanity.framework.libraries.Library;
import org.imanity.framework.libraries.LibraryHandler;
import org.imanity.framework.locale.Locale;
import org.imanity.framework.locale.LocaleHandler;
import org.imanity.framework.locale.LocaleRepository;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.player.IPlayerBridge;
import org.imanity.framework.redis.RedisService;
import org.imanity.framework.redis.server.ServerHandler;
import org.imanity.framework.redis.server.enums.ServerState;
import org.imanity.framework.task.ITaskScheduler;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImanityCommon {

    private static final Set<Library> GLOBAL_LIBRARIES = EnumSet.of(Library.MONGO_DB, Library.HIKARI_CP, Library.YAML, Library.CAFFEINE);
    public static final String METADATA_PREFIX = "Imanity_";

    public static ImanityBridge BRIDGE;
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
    public static MySQL SQL;

    @Autowired
    public static RedisService REDIS;

    @Autowired
    public static ServerHandler SERVER_HANDLER;

    private static boolean LIBRARIES_INITIALIZED, BRIDGE_INITIALIZED;

    public static void init() {
        ImanityCommon.CORE_CONFIG = new CoreConfig();
        ImanityCommon.CORE_CONFIG.loadAndSave();

        ImanityCommon.loadLibraries();
        ClassFactory.loadClasses();
        FieldFactory.loadFields();

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
            EnumSet<Library> guava = EnumSet.of(Library.GUAVA);
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(guava);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(guava);
        }

        try {
            Class.forName("it.unimi.dsi.fastutil.Arrays");
        } catch (ClassNotFoundException ex) {
            EnumSet<Library> guava = EnumSet.of(Library.FAST_UTIL);
            ImanityCommon.LIBRARY_HANDLER.downloadLibraries(guava);
            ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(guava);
        }

        EnumSet<Library> redisson;
        try {
            Class.forName("io.netty.channel.Channel");

            redisson = EnumSet.of(Library.REDISSON_RELOCATED);
        } catch (ClassNotFoundException ex) {
            redisson = EnumSet.of(Library.REDISSON);
        }

        ImanityCommon.LIBRARY_HANDLER.downloadLibraries(redisson);
        ImanityCommon.LIBRARY_HANDLER.obtainClassLoaderWith(redisson);

    }

    public static Logger getLogger() {
        return ImanityCommon.BRIDGE.getLogger();
    }

    public static <T> T getService(Class<T> type) {
        return (T) SERVICE_HANDLER.getServiceInstance(type);
    }

    public static void registerAutowired(Object instance) {
        SERVICE_HANDLER.registerAutowired(instance);
    }

    public static void shutdown() {
        if (ImanityCommon.CORE_CONFIG.USE_REDIS) {
            SERVER_HANDLER.changeServerState(ServerState.STOPPING);
        }

        ImanityCommon.SERVICE_HANDLER.stop();
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

    public static Builder builder() {
        if (ImanityCommon.BRIDGE_INITIALIZED) {
            throw new IllegalStateException("Already build!");
        }

        ImanityCommon.BRIDGE_INITIALIZED = true;
        return new Builder();
    }

    public static class Builder {

        private ImanityBridge bridge;
        private ICommandExecutor commandExecutor;
        private IEventHandler eventHandler;
        private ITaskScheduler taskScheduler;
        private IPlayerBridge playerBridge;
        private ObjectMapper mapper;

        public Builder bridge(ImanityBridge bridge) {
            this.bridge = bridge;
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

        public Builder playerBridge(IPlayerBridge playerBridge) {
            this.playerBridge = playerBridge;
            return this;
        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public void init() {
            if (this.bridge != null) {
                ImanityCommon.BRIDGE = this.bridge;
                FrameworkMisc.BRIDGE = this.bridge;
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
            } else {
                ImanityCommon.JACKSON_MAPPER = this.mapper;
            }

            FrameworkMisc.JACKSON_MAPPER = ImanityCommon.JACKSON_MAPPER;

            ImanityCommon.init();
        }

    }

}
