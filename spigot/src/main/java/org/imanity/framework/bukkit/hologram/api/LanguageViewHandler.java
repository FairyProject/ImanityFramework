package org.imanity.framework.bukkit.hologram.api;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;

import javax.annotation.Nullable;

public class LanguageViewHandler implements ViewHandler {

    private String key;

    @Override
    public String view(@Nullable Player player) {
        return Imanity.translate(player, key);
    }
}
