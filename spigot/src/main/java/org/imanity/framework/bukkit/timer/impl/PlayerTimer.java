package org.imanity.framework.bukkit.timer.impl;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerTimer extends AbstractTimer {

    private final Player player;

    public PlayerTimer(Player player, long beginTime, long duration) {
        super(beginTime, duration);

        this.player = player;
    }

    public PlayerTimer(Player player, long duration) {
        this(player, System.currentTimeMillis(), duration);
    }

}
