/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
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

package org.imanity.framework.bukkit.impl.server;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.HologramSingle;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void sendHologramSpawnPacket(Player player, HologramSingle hologramSingle) {
        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();

        packet.setA(hologramSingle.getSkullId());
        packet.setB(30);
        packet.setC((int) Math.floor(hologramSingle.getLocation().getX() * 32.0));
        packet.setD((int) Math.floor((hologramSingle.getLocation().getY() - 2.25) * 32.0));
        packet.setE((int) Math.floor(hologramSingle.getLocation().getZ() * 32.0));

        DataWatcher dataWatcher = new DataWatcher((net.minecraft.server.v1_8_R3.Entity) null);
        dataWatcher.a(0, (byte) 32);
        dataWatcher.a(2, hologramSingle.getViewHandler().view(player));
        dataWatcher.a(3, (byte) 1);

        packet.setL(dataWatcher);

        MinecraftReflection.sendPacket(player, packet);
    }

    @Override
    public void sendHologramNamePacket(Player player, HologramSingle hologramSingle) {

        DataWatcher dataWatcher = new DataWatcher((net.minecraft.server.v1_8_R3.Entity) null);
        dataWatcher.a(0, (byte) 32);
        dataWatcher.a(2, hologramSingle.getViewHandler().view(player));
        dataWatcher.a(3, (byte) 1);

        MinecraftReflection.sendPacket(player, new PacketPlayOutEntityMetadata(hologramSingle.getSkullId(), dataWatcher, true));

    }
}
