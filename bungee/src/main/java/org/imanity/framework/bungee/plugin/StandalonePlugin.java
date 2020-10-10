package org.imanity.framework.bungee.plugin;

import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;

public class StandalonePlugin extends Plugin {

    @Override
    public void onEnable() {

        for (ImanityPlugin plugin : Imanity.PLUGINS) {
            plugin.preEnable();
        }
        Imanity.init(this);

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
