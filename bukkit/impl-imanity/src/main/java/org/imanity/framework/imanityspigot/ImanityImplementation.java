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
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new ImanityMovementImplementation(movementListener);
    }
}
