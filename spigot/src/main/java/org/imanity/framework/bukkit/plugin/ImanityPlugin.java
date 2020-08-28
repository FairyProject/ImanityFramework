package org.imanity.framework.bukkit.plugin;

import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;

public abstract class ImanityPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        if (this.getDescription().getLoad() != PluginLoadOrder.STARTUP) {
            new IllegalStateException("The ImanityFramework requires the plugin to be load on startup, please add [load: STARTUP] into your plugin.yml!").printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Imanity.PLUGINS.add(this);
    }

    @Override
    public final void onEnable() {
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
