package org.imanity.framework.util.blocks;


import java.util.*;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.Imanity;
import org.imanity.framework.util.CountdownCallback;

public class MultiBlockChanger {

    private String worldName;

    private int maxChanges = 100;

    private boolean async = false;
    private int highestBlock = -1;

    private Runnable callback = null;

    private long tick = 5L;

    private final Queue<BlockChange> blockChanges = new ArrayDeque<>();

    public MultiBlockChanger(World world) {
        this.worldName = world.getName();
    }

    public MultiBlockChanger(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMaxChanges() {
        return maxChanges;
    }

    public boolean isAsync() {
        return async;
    }

    public Runnable getCallback() {
        return callback;
    }

    public long getTick() {
        return tick;
    }

    public Queue<BlockChange> getBlockChanges(){
        return blockChanges;
    }

    public MultiBlockChanger setWorldName(String worldName) {
        this.worldName = worldName;
        return this;
    }

    public MultiBlockChanger setMaxChanges(int maxChanges) {
        this.maxChanges = maxChanges;
        return this;
    }

    public MultiBlockChanger async() {
        this.async = true;
        return this;
    }

    public MultiBlockChanger tick(long tick) {
        this.tick = tick;
        return this;
    }

    public MultiBlockChanger callback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    public MultiBlockChanger highestBlock(int highestBlock) {
        this.highestBlock = highestBlock;
        return this;
    }

    public MultiBlockChanger addBlockChanges(Block block, MaterialData materialData) {
        this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(block), materialData));
        return this;
    }

    public MultiBlockChanger addBlockChanges(Location location, MaterialData materialData) {
        this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(location), materialData));
        return this;
    }

    public MultiBlockChanger addBlockChanges(Block block, Material material, byte data) {
        this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(block), material, data));
        return this;
    }

    public MultiBlockChanger addBlockChanges(Location location, Material material, byte data) {
        this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(location), material, data));
        return this;
    }

    public MultiBlockChanger addBlockChanges(Material material, byte data, Location... locations) {
        for (final Location location : locations) {
            this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(location), material, data));
        }
        return this;
    }

    public MultiBlockChanger addBlockChanges(Material material, byte data, Block... blocks) {
        for (final Block block : blocks) {
            this.blockChanges.add(new BlockChange(BlockVector.toBlockVector(block), material, data));
        }
        return this;
    }

    private BukkitTask bukkitTask = null;

    public void start(JavaPlugin plugin) {

        final World world = Bukkit.getWorld(worldName);

        WorldServer worldServer = ((CraftWorld) world).getHandle();

        List<Long> toLoad = new LongArrayList();

        for (BlockChange blockChange : this.blockChanges) {
            int x = blockChange.getBlockVector().x >> 4, z = blockChange.getBlockVector().z >> 4;
            long key = LongHash.toLong(x, z);
            if (!toLoad.contains(key)) {
                toLoad.add(key);
            }
        }

        Map<Long, Chunk> chunkList = new HashMap<>();
        CountdownCallback countdownCallback = new CountdownCallback(toLoad.size(), () -> {
            final Runnable runnable = new Runnable() {
                private final Queue<BlockChange> blockChanges = new ArrayDeque<>(MultiBlockChanger.this.blockChanges);

                private boolean blockSetDone = false;

                @Override
                public void run() {

                    if (!blockChanges.isEmpty()) {
                        for (int i = 0; i < maxChanges; i++) {

                            if (blockChanges.isEmpty()) {
                                blockSetDone = true;
                                return;
                            }

                            final BlockChange blockChange = blockChanges.poll();
                            BlockVector blockVector = blockChange.getBlockVector();

                            long key = LongHash.toLong(blockVector.getX() >> 4, blockVector.getZ() >> 4);
                            Chunk chunk = chunkList.getOrDefault(key, null);
                            if (chunk == null) {
                                continue;
                            }

                            if (highestBlock > 0) {
                                blockVector = new BlockVector(blockVector);
                                blockVector.y = chunk.b(blockVector.x & 15, blockVector.z & 15);

                                for (int y = 0; y < highestBlock; y++) {
                                    final int combined = blockChange.getMaterialData().getItemTypeId() + (blockChange.getMaterialData().getData() << 12);
                                    final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

                                    ChunkSection cs = chunk.getSections()[blockVector.y >> 4];
                                    cs.setType(blockVector.getX() & 15, blockVector.getY() & 15, blockVector.getZ() & 15, ibd);

                                    blockVector.y++;
                                }
                            } else {

                                final int combined = blockChange.getMaterialData().getItemTypeId() + (blockChange.getMaterialData().getData() << 12);
                                final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

                                ChunkSection cs = chunk.getSections()[blockVector.y >> 4];
                                cs.setType(blockVector.getX() & 15, blockVector.getY() & 15, blockVector.getZ() & 15, ibd);

                            }

                        }
                    }

                    if (!blockSetDone) {
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (BlockChange blockChange : MultiBlockChanger.this.blockChanges) {
                            BlockVector blockVector = blockChange.blockVector;
                            worldServer.notify(new BlockPosition(blockVector.getX() & 15, blockVector.getY() & 15, blockVector.getZ() & 15));
                        }

                        for (Chunk chunk : chunkList.values()) {
                            Imanity.KEEP_CHUNK_HANDLER.removeChunk(chunk.locX, chunk.locZ);
                        }

                        if (callback != null) {
                            callback.run();
                        }
                    });

                    bukkitTask.cancel();
                }
            };

            if (async) {
                bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, tick);
                return;
            }
            bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, runnable, 0L, tick);
        });

        for (long key : toLoad) {
            int x = LongHash.lsw(key), z = LongHash.msw(key);
            worldServer.chunkProviderServer.getChunkAt(x, z, () -> {
                Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(x, z);
                chunkList.put(key, chunk);
                Imanity.KEEP_CHUNK_HANDLER.addChunk(x, z);
                countdownCallback.countdown();
            });
        }
    }

    public static class BlockVector {

        int x = 0, y = 0, z = 0;

        private BlockVector() {}

        public BlockVector(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public BlockVector(BlockVector blockVector) {
            this.x = blockVector.x;
            this.y = blockVector.y;
            this.z = blockVector.z;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        private static BlockVector toBlockVector(Block block) {
            return new BlockVector(block.getX(), block.getY(), block.getZ());
        }

        private static BlockVector toBlockVector(Location location) {
            return new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

    }

    private static class BlockChange {

        private BlockVector blockVector;
        private MaterialData materialData;

        private BlockChange() {}

        public BlockChange(BlockVector blockVector, MaterialData materialData) {
            this.blockVector = blockVector;
            this.materialData = materialData;
        }

        public BlockChange(BlockVector blockVector, Material material, byte data) {
            this.blockVector = blockVector;
            this.materialData = new MaterialData(material, data);
        }

        public BlockVector getBlockVector() {
            return blockVector;
        }

        public MaterialData getMaterialData() {
            return materialData;
        }

    }

}