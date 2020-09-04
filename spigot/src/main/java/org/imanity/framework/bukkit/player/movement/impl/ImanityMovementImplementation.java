package org.imanity.framework.bukkit.player.movement.impl;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

public class ImanityMovementImplementation extends AbstractMovementImplementation {

    private MovementHandler movementHandler;

    public ImanityMovementImplementation(MovementListener movementListener) {
        super(movementListener);
    }

    @Override
    public void register() {
        if (this.movementHandler != null) {
            return;
        }

        movementHandler = new MovementHandler() {
            @Override
            public void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
                updateLocation(player, from, to);
            }

            @Override
            public void handleUpdateRotation(Player player, Location to, Location from, PacketPlayInFlying packetPlayInFlying) {
                updateRotation(player, from, to);
            }
        };

        iSpigot.INSTANCE.addMovementHandler(movementHandler);
    }

    @Override
    public void unregister() {
        if (this.movementHandler == null) {
            return;
        }

        iSpigot.INSTANCE.getMovementHandlers().remove(this.movementHandler);
    }
}
