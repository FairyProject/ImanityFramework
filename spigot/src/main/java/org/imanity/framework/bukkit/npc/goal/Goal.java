package org.imanity.framework.bukkit.npc.goal;

import org.imanity.framework.bukkit.npc.NPC;

public abstract class Goal {

    protected NPC npc;

    public Goal(NPC npc) {
        this.npc = npc;
    }

    public abstract void tick();

}
