package org.imanity.framework.plugin;

import org.jetbrains.annotations.Nullable;

public interface PluginHandler {

    @Nullable
    String getPluginByClass(Class<?> type);

}
