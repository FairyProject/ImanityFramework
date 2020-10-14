package org.imanity.framework.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bungee.impl.*;
import org.imanity.framework.bungee.plugin.ImanityPlugin;
import org.imanity.framework.libraries.classloader.PluginClassLoader;
import org.imanity.framework.plugin.component.ComponentRegistry;
import org.imanity.framework.task.chain.TaskChainFactory;

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
                .bridge(new BungeeImanityBridge())
                .commandExecutor(new BungeeCommandExecutor())
                .eventHandler(new BungeeEventHandler())
                .playerBridge(new BungeePlayerBridge())
                .taskScheduler(new BungeeTaskScheduler())
        .init();
    }

    public static ProxyServer getProxy() {
        return Imanity.PLUGIN.getProxy();
    }

    public static void shutdown() {
        SHUTTING_DOWN = true;

        TASK_CHAIN_FACTORY.shutdown(60, TimeUnit.SECONDS);

        ImanityCommon.shutdown();
    }
}
