package org.imanity.framework.bukkit.plugin;

import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class ImanityPlugin extends JavaPlugin {

    private Queue<Runnable> enableQueues = new ArrayDeque<>();

    public final void queue(Runnable runnable) {
        if (this.isEnabled()) {
            runnable.run();
            return;
        }
        this.enableQueues.add(runnable);
    }

    private final void runQueue() {
        Runnable runnable;
        while ((runnable = this.enableQueues.poll()) != null) {
            runnable.run();
        }
        this.enableQueues = null;
    }

    public ImanityPlugin() {
        Imanity.PLUGINS.add(this);
    }

    @Override
    public final void onLoad() {
        this.load();
    }

    public void load() {

    }

    @Override
    public final void onEnable() {
        this.postEnable();
        this.runQueue();
    }

    @Override
    public final void onDisable() {
    }

    public void preEnable() {

    }

    public void postEnable() {

    }

    public void preDisable() {

    }

    public void postDisable() {

    }

}
