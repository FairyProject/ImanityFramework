package org.imanity.framework.bungee.impl;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;
import org.imanity.framework.bungee.plugin.annotation.PluginProvider;
import org.imanity.framework.bungee.util.PluginUtil;
import org.imanity.framework.plugin.component.ComponentHolder;

public class ComponentHolderBungeeListener extends ComponentHolder {
    @Override
    public Class<?>[] type() {
        return new Class[] {Listener.class};
    }

    @Override
    public Object newInstance(Class<?> type) {
        Plugin plugin = Imanity.PLUGIN;
        Class<? extends Plugin> pluginClass = Plugin.class;

        PluginProvider provider = type.getAnnotation(PluginProvider.class);
        if (provider != null) {
            pluginClass = provider.value();
            plugin = PluginUtil.getPlugin(pluginClass);
        }

        Listener listener;

        constructor: {

            try {

                listener = (Listener) type
                        .getConstructor(pluginClass)
                        .newInstance(plugin);
                break constructor;

            } catch (ReflectiveOperationException ex) {
            }

            try {
                listener = (Listener) type.newInstance();

                break constructor;
            } catch (ReflectiveOperationException ex) {

            }

            throw new RuntimeException("Couldn't find valid constructor for " + type.getSimpleName());
        }

        Imanity.getProxy().getPluginManager().registerListener(plugin, listener);
        return listener;
    }
}
