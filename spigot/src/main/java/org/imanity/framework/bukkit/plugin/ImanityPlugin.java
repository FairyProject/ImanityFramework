package org.imanity.framework.bukkit.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.Imanity;

public abstract class ImanityPlugin extends JavaPlugin {

    @Override
    public final void onEnable() {
        this.preEnable();

        Imanity.init(this);

        this.postEnable();
    }

    @Override
    public final void onDisable() {
        this.preDisable();

        Imanity.shutdown();

        this.postDisable();
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
