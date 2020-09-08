package org.imanity.framework.bukkit.npc.goal;

import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.npc.NPC;

public class LookAtPlayerGoal extends Goal {

    private final int distance;

    public LookAtPlayerGoal(NPC npc, int distance) {
        super(npc);
        this.distance = distance;
    }

    @Override
    public void tick() {

        Player lastPlayer = null;
        double lastDistance = -1;

        for (Player other : npc.getSeeingPlayers()) {

            double distance = other.getLocation().distanceSquared(this.npc.getLocation());
            if (distance < this.distance && (lastPlayer == null || lastDistance > distance)) {

                lastPlayer = other;
                lastDistance = distance;

            }

        }

        if (lastPlayer == null) {
            return;
        }

        this.npc.look(lastPlayer.getLocation());

    }
}
