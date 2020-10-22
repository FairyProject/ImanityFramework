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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.imanity.framework.bukkit.hologram.HologramSingle;
import org.imanity.framework.bukkit.nametag.NameTagInfo;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.visual.VisualPosition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServerImplementation {

    static ServerImplementation load() {
        switch (SpigotUtil.SPIGOT_TYPE) {
            case PAPER:
            case SPIGOT:
            case CRAFTBUKKIT:
                return new NormalImplementation();
            case IMANITY:
                return new ImanityImplementation();
        }
        return null;
    }

    Entity getEntity(UUID uuid);

    Entity getEntity(World world, int id);

    default Entity getEntity(int id) {
        for (World world : Bukkit.getWorlds()) {
            Entity entity = this.getEntity(world, id);
            if (entity != null) {
                return entity;
            }
        }

        return null;
    }

    void showDyingNPC(Player player);

    Object toBlockNMS(MaterialData materialData);

    List<Player> getPlayerRadius(Location location, double radius);

    void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> positions, List<BlockPosition> toRemove, boolean send);

    void clearFakeBlocks(Player player, boolean send);

    void sendActionBar(Player player, String message);

    float getBlockSlipperiness(Material material);

    void sendTeam(Player player, String name, String prefix, String suffix, Collection<String> nameSet, int type);

    void sendMember(Player player, String name, Collection<String> players, int type);

    void sendEntityDestroy(Player player, int... ids);

    void sendEntityTeleport(Player player, Location location, int id);

    void sendEntityAttach(Player player, int type, int toAttach, int attachTo);

    void sendHologramSpawnPacket(Player player, HologramSingle hologramSingle);

    void sendHologramNamePacket(Player player, HologramSingle hologramSingle);
}
