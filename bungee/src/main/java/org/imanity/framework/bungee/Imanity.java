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

package org.imanity.framework.bungee;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bungee.impl.*;
import org.imanity.framework.bungee.plugin.ImanityPlugin;
import org.imanity.framework.ComponentRegistry;
import org.imanity.framework.plugin.PluginClassLoader;
import org.imanity.framework.task.TaskChainFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Imanity {

    public static Plugin PLUGIN;
    public static Logger LOGGER = LogManager.getLogger("Imanity");

    public static PluginClassLoader CLASS_LOADER;
    public static List<ImanityPlugin> PLUGINS;

    public static boolean SHUTTING_DOWN;

    public static TaskChainFactory TASK_CHAIN_FACTORY;

    public static void init(Plugin plugin) {
        Imanity.PLUGIN = plugin;
        Imanity.CLASS_LOADER = new PluginClassLoader(PLUGIN.getClass().getClassLoader());

        Imanity.initCommon();

        TASK_CHAIN_FACTORY = new BungeeTaskChainFactory();
    }

    private static void initCommon() {
        ComponentRegistry.registerComponentHolder(new ComponentHolderBungeeListener());

        ImanityCommon.builder()
                .platform(new BungeeImanityPlatform())
                .commandExecutor(new BungeeCommandExecutor())
                .eventHandler(new BungeeEventHandler())
                .taskScheduler(new BungeeTaskScheduler())
        .init();
    }

    public static ProxyServer getProxy() {
        return Imanity.PLUGIN.getProxy();
    }

    @SneakyThrows
    public static void shutdown() {
        SHUTTING_DOWN = true;

        TASK_CHAIN_FACTORY.shutdown(60, TimeUnit.SECONDS);

        ImanityCommon.shutdown();
    }
}
