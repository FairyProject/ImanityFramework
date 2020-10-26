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

package org.imanity.framework.bukkit.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.util.Terminable;

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class ImanityPlugin extends JavaPlugin implements Terminable {

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

    @Override
    public void close() throws Exception {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public boolean isClosed() {
        return this.isEnabled();
    }

}
