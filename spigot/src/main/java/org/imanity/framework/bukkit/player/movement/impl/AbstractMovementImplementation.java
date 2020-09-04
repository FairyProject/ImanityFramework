package org.imanity.framework.bukkit.player.movement.impl;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.bukkit.player.movement.MovementListener;

@Getter
public abstract class AbstractMovementImplementation {

    private MovementListener movementListener;
    private boolean ignoreSameBlock;
    private boolean ignoreSameY;

    public AbstractMovementImplementation(MovementListener movementListener) {
        this.movementListener = movementListener;
    }

    public AbstractMovementImplementation ignoreSameBlock() {
        this.ignoreSameBlock = true;
        return this;
    }

    public AbstractMovementImplementation ignoreSameBlockAndY() {
        this.ignoreSameBlock = true;
        this.ignoreSameY = true;
        return this;
    }

    public void register() {

    }

    public void unregister() {

    }

    public void updateLocation(Player player, Location from, Location to) {
        if (from.getX() != to.getX()
                || from.getY() != to.getY()
                || from.getZ() != to.getZ()) {
            boolean cancelled = false;

            if (this.isIgnoreSameBlock() && from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
                cancelled = true;
            } else if (this.isIgnoreSameY() && from.getX() == to.getX() && from.getZ() == to.getZ()) {
                cancelled = true;
            }

            if (!cancelled) {
                this.movementListener.handleUpdateLocation(player, from, to);
            }
        }
    }

    public void updateRotation(Player player, Location from, Location to) {
        if (from.getYaw() != to.getYaw()
                || from.getPitch() != to.getPitch()) {
            this.movementListener.handleUpdateRotation(player, from, to);
        }
    }

}
