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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.plugin.PluginHandler;
import org.imanity.framework.reflect.ReflectObject;
import org.jetbrains.annotations.Nullable;

public class BungeePluginHandler implements PluginHandler {

    private final Class<?> pluginLoaderClass;

    public BungeePluginHandler() {
        try {
            pluginLoaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("net.md_5.bungee.api.plugin.PluginClassloader wasn't found!");
        }
    }

    @Override
    public @Nullable String getPluginByClass(Class<?> type) {
        try {
            ClassLoader classLoader = type.getClassLoader();
            if (!pluginLoaderClass.isInstance(classLoader)) {
                return Imanity.PLUGIN.getDescription().getName();
            }

            ReflectObject reflectObject = new ReflectObject(classLoader);
            Plugin plugin = reflectObject.get("plugin");
            return plugin.getDescription().getName();
        } catch (Throwable ignored) {
            return Imanity.PLUGIN.getDescription().getName();
        }
    }
}
