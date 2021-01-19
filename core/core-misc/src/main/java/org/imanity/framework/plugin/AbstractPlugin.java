package org.imanity.framework.plugin;

public interface AbstractPlugin {

    default void onInitial() {

    }

    default void onPreEnable() {

    }

    default void onPluginEnable() {

    }

    default void onPluginDisable() {

    }

    default void onFrameworkFullyDisable() {

    }

    ClassLoader getPluginClassLoader();

    String getName();

}
