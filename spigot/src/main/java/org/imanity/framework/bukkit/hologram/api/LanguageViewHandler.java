package org.imanity.framework.bukkit.hologram.api;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;

import javax.annotation.Nullable;

@RequiredArgsConstructor
public class LanguageViewHandler implements ViewHandler {

    private final String key;

    @Override
    public String view(@Nullable Player player) {
        if (player == null) {
            return key;
        }
        return Imanity.translate(player, key);
    }
}
