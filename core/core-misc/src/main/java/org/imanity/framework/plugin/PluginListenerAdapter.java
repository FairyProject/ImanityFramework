package org.imanity.framework.plugin;

public interface PluginListenerAdapter {

    default void onPluginEnable(AbstractPlugin plugin) {

    }

    default void onPluginDisable(AbstractPlugin plugin) {

    }

}
