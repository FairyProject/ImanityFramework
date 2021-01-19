package org.imanity.framework.bukkit.plugin;

import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BukkitPlugin extends JavaPlugin implements AbstractPlugin {

    @Override
    public final void onLoad() {
        this.initial();

        this.onInitial();
    }

    @Override
    public final void onEnable() {
        this.runStartupQueue();
        this.onPreEnable();

        PluginManager.INSTANCE.onPluginEnable(this);

        this.onPluginEnable();
    }

    @Override
    public final void onDisable() {
        this.onPluginDisable();

        PluginManager.INSTANCE.onPluginDisable(this);
    }

    @Override
    public final ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }

    private Queue<Runnable> startupQueue = new ConcurrentLinkedQueue<>();

    public final void addStartupQueue(Runnable runnable) {
        this.startupQueue.add(runnable);
    }

    private void runStartupQueue() {
        this.startupQueue.forEach(Runnable::run);
        this.startupQueue.clear();
        this.startupQueue = null;
    }

    private void initial() {
        if (this.getDescription().getLoad() != PluginLoadOrder.STARTUP) {
            this.getLogger().warning("The Load Order is not START UP! (Did you modified config.yml?)");

            this.setEnabled(false);
        }

        PluginManager.INSTANCE.addPlugin(this);
    }

}
