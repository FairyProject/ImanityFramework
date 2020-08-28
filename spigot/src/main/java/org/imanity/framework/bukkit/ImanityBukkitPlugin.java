package org.imanity.framework.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;

public final class ImanityBukkitPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            plugin.preEnable();
        }

        Imanity.init(this);

        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            plugin.postEnable();
        }

    }

    @Override
    public void onDisable() {

        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            plugin.preDisable();
        }

        Imanity.shutdown();

        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            plugin.postDisable();
        }

    }
}
