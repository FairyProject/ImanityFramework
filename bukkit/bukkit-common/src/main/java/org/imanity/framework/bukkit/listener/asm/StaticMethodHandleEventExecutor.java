/*
 * Copyright (C) Imanity - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Only authorized persons are qualified to use and view the source code
 *  * Proprietary and confidential
 *  * Written by LeeGod <leegod@imanity.dev>, Jan 2021
 */

package org.imanity.framework.bukkit.listener.asm;

import com.google.common.base.Preconditions;
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
import java.lang.reflect.Modifier;

public class StaticMethodHandleEventExecutor implements EventExecutor {

    private final boolean ignoredFilters;
    private final FilteredEventList eventList;
    private final Class<? extends Event> eventClass;
    private final MethodHandle handle;

    public StaticMethodHandleEventExecutor(@NonNull Class<? extends Event> eventClass, @NonNull Method m, boolean ignoredFilters, FilteredEventList eventList) {
        Preconditions.checkArgument(Modifier.isStatic(m.getModifiers()), "Not a static method: %s", m);
        Preconditions.checkArgument(eventClass != null, "eventClass is null");
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
            handle.invoke(event);
        }
    }
}
