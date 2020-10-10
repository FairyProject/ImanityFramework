package org.imanity.framework.bungee.util;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.imanity.framework.bungee.Imanity;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class PluginUtil {

    @Nullable
    public static <T extends Plugin> T getPlugin(Class<T> type) {
        for (Plugin plugin : Imanity.getProxy().getPluginManager().getPlugins()) {
            if (type.isInstance(plugin)) {
                return type.cast(plugin);
            }
        }

        return null;
    }

}
