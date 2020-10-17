package org.imanity.framework.bukkit.util.blocks;


import java.util.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.util.CountdownCallback;

public class MultiBlockChanger {

    private String worldName;

    private int maxChanges = 100;
    private int maxChunkPerTick = 10;

    private boolean async = false;
    private int highestBlock = -1;

    private Runnable callback = null;

    private long tick = 5L;

    private final List<BlockChange> blockChanges = new ArrayList<>();

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

    public List<BlockChange> getBlockChanges(){
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

    public MultiBlockChanger maxChunkPerTick(int chunks) {
        this.maxChunkPerTick = chunks;
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

        Set<QueuedChunk> toLoad = new ObjectOpenHashSet<>();

        for (BlockChange blockChange : this.blockChanges) {
            int x = blockChange.getBlockVector().x >> 4, z = blockChange.getBlockVector().z >> 4;
            for (int cx = x - 1; cx < x + 1; cx++) {
                for (int cz = z - 1; cz < z + 1; cz++) {
                    toLoad.add(new QueuedChunk(cx, cz, cx == x && cz == z));
                }
            }
        }

        System.out.println(this.calculateTimeToBuild(false) + "ms");
        long start = System.currentTimeMillis();

        Map<Long, Chunk> chunkList = new HashMap<>();
        CountdownCallback countdownCallback = new CountdownCallback(toLoad.size(), () -> {

            final Runnable runnable = new Runnable() {
                private final BlockChange[] blockChanges = MultiBlockChanger.this.blockChanges.toArray(new BlockChange[0]);

                private int index = 0;
                private boolean blockSetDone = false;

                @Override
                public void run() {

                    if (index < blockChanges.length) {
                        BlockPosition.MutableBlockPosition mutableBlockPosition = new BlockPosition.MutableBlockPosition();

                        for (int i = 0; i < maxChanges; i++) {

                            if (index >= blockChanges.length) {
                                blockSetDone = true;
                                return;
                            }

                            final BlockChange blockChange = blockChanges[index++];
                            BlockVector blockVector = blockChange.getBlockVector();

                            long key = LongHash.toLong(blockVector.getX() >> 4, blockVector.getZ() >> 4);
                            Chunk chunk = chunkList.getOrDefault(key, null);
                            if (chunk == null) {
                                System.out.println("chunk doesn't exists");
                                continue;
                            }

                            if (highestBlock > 0) {
                                blockVector = new BlockVector(blockVector);
                                mutableBlockPosition.c(blockVector.x, chunk.b(blockVector.x & 15, blockVector.z & 15), blockVector.z);

                                for (int y = 0; y < highestBlock; y++) {
                                    final int combined = blockChange.materialData.getItemTypeId() + (blockChange.materialData.getData() << 12);
                                    final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);
//                                    worldServer.setTypeAndData(mutableBlockPosition, ibd, 2);
//                                    chunk.a(mutableBlockPosition, ibd);

                                    ChunkSection cs = chunk.getSections()[mutableBlockPosition.getY() >> 4];
                                    if (cs == null) {
                                        cs = new ChunkSection(mutableBlockPosition.getY() >> 4 << 4, !worldServer.worldProvider.o());
                                        chunk.getSections()[mutableBlockPosition.getY() >> 4] = cs;
                                    }
                                    cs.setType(mutableBlockPosition.getX() & 15, mutableBlockPosition.getY() & 15, mutableBlockPosition.getZ() & 15, ibd);

                                    mutableBlockPosition.setY(mutableBlockPosition.getY() + 1);
                                }
                            } else {

                                final int combined = blockChange.getMaterialData().getItemTypeId() + (blockChange.getMaterialData().getData() << 12);
                                final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

                                ChunkSection cs = chunk.getSections()[blockVector.y >> 4];
                                if (cs == null) {
                                    cs = new ChunkSection(blockVector.y >> 4 << 4, !worldServer.worldProvider.o());
                                    chunk.getSections()[blockVector.y >> 4] = cs;
                                }
                                cs.setType(blockVector.getX() & 15, blockVector.getY() & 15, blockVector.getZ() & 15, ibd);

                            }

                        }
                    }

                    System.out.println(index + "/" + blockChanges.length);

                    if (!blockSetDone) {
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (BlockChange blockChange : MultiBlockChanger.this.blockChanges) {
                            BlockVector blockVector = blockChange.blockVector;

                            if (highestBlock > 0) {
                                blockVector = new BlockVector(blockVector);
                                blockVector.y = worldServer.getHighestBlockYAt(new BlockPosition(blockVector.x, blockVector.y, blockVector.z)).getY();

                                for (int y = 0; y < highestBlock; y++) {
                                    worldServer.notify(new BlockPosition(blockVector.getX(), blockVector.getY(), blockVector.getZ()));
                                    blockVector.y++;
                                }
                            } else {
                                worldServer.notify(new BlockPosition(blockVector.getX(), blockVector.getY(), blockVector.getZ()));
                            }
                        }

                        for (Chunk chunk : chunkList.values()) {
                            chunk.initLighting();
                            Imanity.KEEP_CHUNK_HANDLER.removeChunk(chunk.locX, chunk.locZ);
                        }

                        System.out.println((System.currentTimeMillis() - start) + "ms done");

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

        if (maxChunkPerTick > 0) {
            Iterator<QueuedChunk> iterator = toLoad.iterator();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!iterator.hasNext()) {
                        this.cancel();
                    }

                    for (int i = 0; iterator.hasNext() && i < maxChunkPerTick; i++) {
                        QueuedChunk queuedChunk = iterator.next();

                        worldServer.chunkProviderServer.getChunkAt(queuedChunk.x, queuedChunk.z, () -> {
                            Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(queuedChunk.x, queuedChunk.z);
                            chunkList.put(LongHash.toLong(queuedChunk.x, queuedChunk.z), chunk);
                            if (!chunk.isDone()) {
                                chunk.loadNearby(worldServer.chunkProviderServer, worldServer.chunkProviderServer, queuedChunk.x, queuedChunk.z);
                            }
                            if (queuedChunk.cache) {
                                chunk.initLighting();
                                Imanity.KEEP_CHUNK_HANDLER.addChunk(queuedChunk.x, queuedChunk.z);
                            }
                            countdownCallback.countdown();
                        });
                    }
                }

            }.runTaskTimer(plugin, 1L, 1L);
        } else {
            for (QueuedChunk queuedChunk : toLoad) {
                worldServer.chunkProviderServer.getChunkAt(queuedChunk.x, queuedChunk.z, () -> {
                    Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(queuedChunk.x, queuedChunk.z);
                    chunkList.put(LongHash.toLong(queuedChunk.x, queuedChunk.z), chunk);
                    if (!chunk.isDone()) {
                        chunk.loadNearby(worldServer.chunkProviderServer, worldServer.chunkProviderServer, queuedChunk.x, queuedChunk.z);
                    }
                    if (queuedChunk.cache) {
                        Imanity.KEEP_CHUNK_HANDLER.addChunk(queuedChunk.x, queuedChunk.z);
                    }
                    countdownCallback.countdown();
                });
            }
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

    @AllArgsConstructor
    @Getter
    private static class QueuedChunk {

        private int x;
        private int z;
        private boolean cache;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueuedChunk that = (QueuedChunk) o;

            if (x != that.x) return false;
            return z == that.z;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + z;
            return result;
        }
    }

    public long calculateTimeToBuild(boolean chunkPreloaded) {

        long buildTime = (blockChanges.size() / maxChanges) * tick * 50;

        if (chunkPreloaded) {
            return this.maxChunkPerTick * 50 + buildTime;
        }

        return (this.maxChunkPerTick * 50) + ((blockChanges.size() / 16) * 25) + buildTime;

    }

}