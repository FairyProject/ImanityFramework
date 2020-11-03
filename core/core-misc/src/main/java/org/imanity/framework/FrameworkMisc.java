package org.imanity.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.imanity.framework.events.IEventHandler;
import org.imanity.framework.task.ITaskScheduler;

public class FrameworkMisc {

    public static ImanityBridge BRIDGE;
    public static ITaskScheduler TASK_SCHEDULER;
    public static IEventHandler EVENT_HANDLER;
    public static ObjectMapper JACKSON_MAPPER;

}
