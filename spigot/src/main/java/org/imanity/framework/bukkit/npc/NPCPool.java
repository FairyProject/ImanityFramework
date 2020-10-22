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

package org.imanity.framework.bukkit.npc;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.imanity.framework.bukkit.npc.event.PlayerNPCInteractEvent;
import org.imanity.framework.bukkit.npc.modifier.AnimationModifier;
import org.imanity.framework.bukkit.npc.modifier.MetadataModifier;
import org.imanity.framework.bukkit.npc.util.AxisAlignedBB;
import org.imanity.framework.bukkit.plugin.ImanityPlugin;
import org.imanity.framework.bukkit.util.CoordXZ;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.bukkit.util.chunk.CachedChunk;
import org.imanity.framework.bukkit.util.chunk.CraftCachedChunk;
import org.imanity.framework.plugin.component.Component;
import org.imanity.framework.util.thread.ServerThreadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import protocolsupport.libs.org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Component
public class NPCPool implements Listener {

    private static final Map<String, NPCPool> NPC_POOLS = new HashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("NPC-Render-Pool-%d")
            .setDaemon(true)
            .build());

    public static NPCPool getPool(String name) {
        return NPC_POOLS.get(name);
    }

    private final JavaPlugin javaPlugin;

    private final World world;

    private final CoordXZ top;
    private final CoordXZ bottom;

    private final double spawnDistance;
    private final double actionDistance;
    private final long tabListRemoveTicks;
    private final Map<Integer, NPC> npcMap = new ConcurrentHashMap<>();
    private final Map<Long, CachedChunk> chunkSnapshots = new ConcurrentHashMap<>();

    private long xzToKey(int x, int z) {
        return ((long) x << 32) + z - Integer.MIN_VALUE;
    }

    private CoordXZ keyToXZ(long key) {
        int x = (int) (key >> 32);
        int z = (int) (key & 0xFFFFFFFF) + Integer.MIN_VALUE;
        return new CoordXZ(x, z);
    }

    private NPCPool(@NotNull JavaPlugin javaPlugin, @NotNull World world, int spawnDistance, int actionDistance, long tabListRemoveTicks, @NotNull CoordXZ corner1, @NotNull CoordXZ corner2) {
        Preconditions.checkArgument(spawnDistance > 0 && actionDistance > 0, "Distance has to be > 0!");
        Preconditions.checkArgument(actionDistance <= spawnDistance, "Action distance cannot be higher than spawn distance!");
        Preconditions.checkArgument(tabListRemoveTicks > 0, "TabListRemoveTicks have to be > 0!");

        this.javaPlugin = javaPlugin;
        this.world = world;

        this.spawnDistance = spawnDistance * spawnDistance;
        this.actionDistance = actionDistance * actionDistance;
        this.tabListRemoveTicks = tabListRemoveTicks;

        this.addInteractListener();
        this.npcTick();

        this.top = new CoordXZ(
                Math.max(corner1.x, corner2.x),
                Math.max(corner1.z, corner2.z)
        );
        this.bottom = new CoordXZ(
                Math.min(corner1.x, corner2.x),
                Math.min(corner1.z, corner2.z)
        );

        this.cacheChunks();
    }

    @Nullable
    public CachedChunk getChunkAt(int x, int z) {
        return this.chunkSnapshots.getOrDefault(this.xzToKey(x >> 4, z >> 4), null);
    }

    public MaterialData getBlockAt(Location location) {

        CachedChunk chunk = this.getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);

        if (chunk != null) {
            int cx = location.getBlockX() & 0xF;
            int cz = location.getBlockZ() & 0xF;
            return chunk.getBlock(cx, location.getBlockY(), cz);
        }

        return null;

    }

    public MaterialData getBlockAt(int x, int y, int z) {

        CachedChunk chunk = this.getChunkAt(x >> 4, z >> 4);

        if (chunk != null) {
            int cx = x & 0xF;
            int cz = z & 0xF;
            return chunk.getBlock(cx, y, cz);
        }

        return null;

    }

    public boolean isMaterialInBoundingBox(AxisAlignedBB boundingBox, Material... materials) {
        int minX = (int) Math.floor(boundingBox.minX), maxX = (int) Math.floor(boundingBox.maxX) + 1,
                minY = (int) Math.floor(boundingBox.minY), maxY = (int) Math.floor(boundingBox.maxY) + 1,
                minZ = (int) Math.floor(boundingBox.minZ), maxZ = (int) Math.floor(boundingBox.maxZ) + 1;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {

                    MaterialData materialData = this.getBlockAt(new Location(this.world, x, y, z));
                    if (materialData != null) {
                        for (Material material : materials) {
                            if (material == materialData.getItemType()) {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    public void setBlockAt(Location location, Material material, byte data) {

        // TODO

    }

    private void cacheChunks() {

        for (int x = this.bottom.x; x < this.top.x; x++) {
            for (int z = this.bottom.z; z < this.top.z; z++) {

                Chunk chunk = this.world.getChunkAt(x, z);
                this.chunkSnapshots.put(this.xzToKey(x, z), CraftCachedChunk.from(chunk.getChunkSnapshot()));

            }
        }

    }

    private void addInteractListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.javaPlugin, PacketType.Play.Client.USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packetContainer = event.getPacket();
                int targetId = packetContainer.getIntegers().read(0);

                if (npcMap.containsKey(targetId)) {
                    NPC npc = npcMap.get(targetId);
                    EnumWrappers.EntityUseAction action = packetContainer.getEntityUseActions().read(0);

                    try (ServerThreadLock lock = ServerThreadLock.obtain()) {
                        Bukkit.getPluginManager().callEvent(new PlayerNPCInteractEvent(event.getPlayer(), npc, action));
                    }
                }
            }

        });
    }

    private void npcTick() {

        TaskUtil.runRepeated(() -> {
            List<NPC> npcs = ImmutableList.copyOf(this.npcMap.values());

            for (NPC npc : npcs) {
                npc.tick();
            }

            EXECUTOR_SERVICE.submit(() -> {
                for (NPC npc : npcs) {
                    npc.render();
                }
            });
        }, 1);

    }

    protected void takeCareOf(@NotNull NPC npc) {
        this.npcMap.put(npc.getEntityId(), npc);
    }

    @Nullable
    public NPC getNPC(int entityId) {
        return this.npcMap.get(entityId);
    }

    public void removeNPC(int entityId) {
        NPC npc = this.getNPC(entityId);

        if (npc != null) {
            this.npcMap.remove(entityId);
            npc.getSeeingPlayers().forEach(npc::hide);
        }
    }

    @EventHandler
    public void handleQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.npcMap.values().stream()
                .filter(npc -> npc.isShownFor(player))
                .forEach(npc -> npc.untrack(player));
    }

    @EventHandler
    public void handleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        this.npcMap.values().stream()
                .filter(npc -> npc.isImitatePlayer() && npc.isShownFor(player) && npc.getLocation().distanceSquared(player.getLocation()) <= this.actionDistance)
                .forEach(npc -> npc.metadata().queue(MetadataModifier.EntityMetadata.SNEAKING, event.isSneaking()).send(player));
    }

    @EventHandler
    public void handleClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.npcMap.values().stream()
                    .filter(npc -> npc.isImitatePlayer() && npc.isShownFor(player) && npc.getLocation().distanceSquared(player.getLocation()) <= this.actionDistance)
                    .forEach(npc -> npc.animation().queue(AnimationModifier.EntityAnimation.SWING_MAIN_ARM).send(player));
        }
    }

    /**
     * @return a copy of the NPCs this pool manages
     */
    public Collection<NPC> getNPCs() {
        return new HashSet<>(this.npcMap.values());
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private ImanityPlugin plugin;
        private String name;
        private World world;
        private CoordXZ cornerA;
        private CoordXZ cornerB;
        private int spawnDistance;
        private int actionDistance;
        private long tabListRemoveTicks;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder plugin(ImanityPlugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder world(World world) {
            this.world = world;
            return this;
        }

        public Builder cornerA(CoordXZ coord) {
            this.cornerA = coord;
            return this;
        }

        public Builder cornerB(CoordXZ coord) {
            this.cornerB = coord;
            return this;
        }

        public Builder range(CoordXZ coord, int range) {
            this.cornerA = new CoordXZ(coord.x + range, coord.z + range);
            this.cornerB = new CoordXZ(coord.x - range, coord.z - range);
            return this;
        }

        public Builder spawnDistance(int spawnDistance) {
            this.spawnDistance = spawnDistance;
            return this;
        }

        public Builder actionDistance(int actionDistance) {
            this.actionDistance = actionDistance;
            return this;
        }

        public Builder tabListRemoveTicks(long tabListRemoveTicks) {
            this.tabListRemoveTicks = tabListRemoveTicks;
            return this;
        }

        public void build() {
            Preconditions.checkArgument(ObjectUtils.allNotNull(name, plugin, world, cornerA, cornerB, spawnDistance, actionDistance, tabListRemoveTicks), "One of field does not exists!");

            NPCPool npcPool = new NPCPool(plugin, world, spawnDistance, actionDistance, tabListRemoveTicks, cornerA, cornerB);
            NPCPool.NPC_POOLS.put(name, npcPool);
        }

    }

}
