package org.imanity.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imanity.framework.aspect.AsyncAspect;
import org.imanity.framework.cache.CacheableAspect;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.libraries.LibraryHandler;
import org.imanity.framework.task.ITaskScheduler;

public class FrameworkMisc {

    public static ImanityPlatform PLATFORM;
    public static ITaskScheduler TASK_SCHEDULER;
    public static IEventHandler EVENT_HANDLER;
    public static ObjectMapper JACKSON_MAPPER;
    public static LibraryHandler LIBRARY_HANDLER;

    public static void close() {
        AsyncAspect.EXECUTOR.shutdown();

        CacheableAspect.CLEANER_SERVICE.shutdown();
        CacheableAspect.UPDATER_SERVICE.shutdown();
    }

}
