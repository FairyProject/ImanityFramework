package org.imanity.framework.bungee.plugin.annotation;

import net.md_5.bungee.api.plugin.Plugin;

public @interface PluginProvider {

    Class<? extends Plugin> value();

}
