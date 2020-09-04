package org.imanity.framework.bukkit.player.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface MovementListener {

    void handleUpdateLocation(Player player, Location from, Location to);

    void handleUpdateRotation(Player player, Location from, Location to);

}
