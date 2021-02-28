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

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.HologramSingle;
import org.imanity.framework.bukkit.impl.annotation.ProviderTestImpl;
import org.imanity.framework.bukkit.impl.annotation.ServerImpl;
import org.imanity.framework.bukkit.impl.server.NormalImplementation;
import org.imanity.framework.bukkit.player.movement.MovementListener;
import org.imanity.framework.bukkit.player.movement.impl.AbstractMovementImplementation;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.reflect.ReflectObject;
import spg.lgdev.config.iSpigotConfig;
import spg.lgdev.handler.MovementHandler;
import spg.lgdev.iSpigot;

import java.util.UUID;

@ServerImpl
@ProviderTestImpl(ImanitySpigotTestImpl.class)
public class ImanityImplementation extends NormalImplementation {

    @Override
    public Entity getEntity(UUID uuid) {
        return MinecraftServer
                .getServer()
                .a(uuid)
                .getBukkitEntity();
    }

    @Override
    public float getBlockSlipperiness(Material material) {
        return Block.getById(material.getId()).frictionFactor;
    }

    @Override
    public boolean isServerThread() {
        return Bukkit.isPrimaryThread(false);
    }

    @Override
    public boolean callMoveEvent(Player player, Location from, Location to) {
        double delta = Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2) + Math.pow(from.getZ() - to.getZ(), 2);
        float deltaAngle = Math.abs(from.getYaw() - to.getYaw()) + Math.abs(from.getPitch() - to.getPitch());

        PacketPlayInFlying flying = new PacketPlayInFlying();
        ReflectObject reflectObject = new ReflectObject(flying);
        if (delta > 0) {
            reflectObject.set("x", to.getX());
            reflectObject.set("y", to.getY());
            reflectObject.set("z", to.getZ());
            reflectObject.set("hasPos", true);
        }
        if (deltaAngle > 0) {
            reflectObject.set("yaw", to.getYaw());
            reflectObject.set("pitch", to.getPitch());
            reflectObject.set("hasPitch", true);
        }
        reflectObject.set("f", player.isOnGround());


        if (delta > 0.0D && !player.isDead()) {
            for (MovementHandler handler : iSpigot.INSTANCE.getMovementHandlers()) {
                try {
                    handler.handleUpdateLocation(player, to, from, flying);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        if (deltaAngle > 0.0F && !player.isDead()) {
            for (MovementHandler handler : iSpigot.INSTANCE.getMovementHandlers()) {
                try {
                    handler.handleUpdateRotation(player, to, from, flying);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (iSpigotConfig.use_player_move_event) {
            return super.callMoveEvent(player, from, to);
        }
        return false;
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new ImanityMovementImplementation(movementListener);
    }
}
