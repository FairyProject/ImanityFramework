package org.imanity.framework.bukkit.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DirectionUtil
{
    public static Direction getDirection(final Player player, final Location location) {
        final Vector direction = player.getEyeLocation().getDirection().setY(0);
        final Vector target = location.toVector().subtract(player.getLocation().toVector()).normalize().setY(0);
        double n;
        for (n = Math.toDegrees(direction.angle(target)); n < 0.0; n += 360.0) {}
        if (n <= 45.0) {
            return Direction.UP;
        }
        if (n > 45.0 && n <= 135.0) {
            if (target.crossProduct(direction).getY() > 0.0) {
                return Direction.RIGHT;
            }
            return Direction.LEFT;
        }
        else {
            if (n > 135.0) {
                return Direction.DOWN;
            }
            return null;
        }
    }

    public enum Direction
    {
        DOWN("\u2193"),
        UP("\u2191"),
        RIGHT("\u2192"),
        LEFT("\u2190");

        @Getter
        private final String text;

        Direction(String text) {
            this.text = text;
        }
    }
}
