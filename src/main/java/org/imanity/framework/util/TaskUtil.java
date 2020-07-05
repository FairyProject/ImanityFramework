package org.imanity.framework.util;

import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.Imanity;

public class TaskUtil {

    public static BukkitTask runAsync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskAsynchronously(Imanity.PLUGIN, runnable);
    }

    public static BukkitTask runAsyncScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLaterAsynchronously(Imanity.PLUGIN, runnable, time);
    }

    public static BukkitTask runAsyncRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimerAsynchronously(Imanity.PLUGIN, runnable, time, time);
    }

    public static BukkitTask runSync(Runnable runnable) {
        return Imanity.PLUGIN.getServer().getScheduler().runTask(Imanity.PLUGIN, runnable);
    }

    public static BukkitTask runScheduled(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskLater(Imanity.PLUGIN, runnable, time);
    }

    public static BukkitTask runRepeated(Runnable runnable, long time) {
        return Imanity.PLUGIN.getServer().getScheduler().runTaskTimer(Imanity.PLUGIN, runnable, time, time);
    }

}
