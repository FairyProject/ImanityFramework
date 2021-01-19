package org.imanity.framework.bukkit.impl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.plugin.PluginHandler;
import org.imanity.framework.reflect.ReflectObject;
import org.jetbrains.annotations.Nullable;

public class BukkitPluginHandler implements PluginHandler {

    @Override
    public @Nullable String getPluginByClass(Class<?> type) {
        try {
            JavaPlugin plugin = JavaPlugin.getProvidingPlugin(type);
            if (plugin != null) {
                return plugin.getName();
            }
        } catch (Throwable ignored) {}

        try {
            ClassLoader classLoader = type.getClassLoader();
            ReflectObject reflectObject = new ReflectObject(classLoader);

            Plugin plugin = reflectObject.get("plugin");
            return plugin.getName();
        } catch (Throwable ignored) {
            return Imanity.PLUGIN.getName();
        }
    }

}
