package org.imanity.framework.bungee.plugin;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class ImanityPlugin extends Plugin {

    private Queue<Runnable> enableQueues = new ArrayDeque<>();
    @Getter
    private boolean enabled;

    public final void queue(Runnable runnable) {
        if (this.enabled) {
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
        this.enabled = true;

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
