package org.imanity.framework.bukkit.visibility;

import org.bukkit.entity.Player;

public interface VisibilityAdapter {

    VisibilityOption check(Player player, Player target);

}
