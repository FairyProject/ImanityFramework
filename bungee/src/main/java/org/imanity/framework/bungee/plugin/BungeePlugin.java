/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.bungee.plugin;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.plugin.AbstractPlugin;
import org.imanity.framework.plugin.PluginManager;
import org.imanity.framework.util.Utility;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class BungeePlugin extends Plugin implements AbstractPlugin {

    @Override
    public final void onLoad() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getPluginClassLoader());
        PluginManager.INSTANCE.addPlugin(this);
        PluginManager.INSTANCE.onPluginInitial(this);

        this.onInitial();
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void onEnable() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getPluginClassLoader());

        Utility.resolveLinkageError();

        this.onPreEnable();

        PluginManager.INSTANCE.onPluginEnable(this);

        this.onPluginEnable();
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void onDisable() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getPluginClassLoader());
        this.onPluginDisable();

        PluginManager.INSTANCE.onPluginDisable(this);
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @Override
    public final void close() {
        // ?
    }

    @Override
    public final ClassLoader getPluginClassLoader() {
        return this.getClass().getClassLoader();
    }

}
