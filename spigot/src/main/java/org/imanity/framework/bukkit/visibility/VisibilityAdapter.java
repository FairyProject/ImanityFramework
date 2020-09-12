package org.imanity.framework.bukkit.visibility;

import org.bukkit.entity.Player;

public interface VisibilityAdapter {

    default boolean shouldShow(Player player, Player target) {
        return false;
    }

    default boolean shouldHide(Player player, Player target) {
        return false;
    }

}
