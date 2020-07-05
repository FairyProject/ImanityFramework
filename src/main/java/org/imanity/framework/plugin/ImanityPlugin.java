package org.imanity.framework.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.Imanity;

public abstract class ImanityPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.preEnable();

        Imanity.init(this);

        this.postEnable();
    }

    @Override
    public void onDisable() {
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
