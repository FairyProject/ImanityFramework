/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.imanityspigot;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.player.movement.impl.AbstractMovementImplementation;
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
