/*
 * Copyright (C) Imanity - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Only authorized persons are qualified to use and view the source code
 *  * Proprietary and confidential
 *  * Written by LeeGod <leegod@imanity.dev>, Jan 2021
 */

package org.imanity.framework.bukkit.listener.asm;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.imanity.framework.bukkit.listener.FilteredEventList;
import org.imanity.framework.reflect.Reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public class MethodHandleEventExecutor implements EventExecutor {

    private final boolean ignoredFilters;
    private final FilteredEventList eventList;
    private final Class<? extends Event> eventClass;
    private final MethodHandle handle;

    public MethodHandleEventExecutor(@NonNull Class<? extends Event> eventClass, @NonNull Method m, boolean ignoredFilters, FilteredEventList eventList) {
        this.eventClass = eventClass;
        this.ignoredFilters = ignoredFilters;
        this.eventList = eventList;
        try {
            m.setAccessible(true);
            this.handle = Reflect.lookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Unable to set accessible", e);
        }
    }

    @Override
    @SneakyThrows
    public void execute(@NonNull Listener listener, @NonNull Event event) throws EventException {
        if (eventClass.isInstance(event) && (ignoredFilters || eventList.check(event))) {
            handle.invoke(listener, event);
        }
    }
}
