package org.imanity.framework.bukkit.plugin;

import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;

public abstract class ImanityPlugin extends JavaPlugin {

    @Override
    public final void onLoad() {
        Imanity.PLUGINS.add(this);
        this.load();
    }

    public void load() {

    }

    @Override
    public final void onEnable() {
        this.postEnable();
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
