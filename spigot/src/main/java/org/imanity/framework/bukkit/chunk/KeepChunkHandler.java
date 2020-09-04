package org.imanity.framework.bukkit.chunk;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.imanity.framework.bukkit.Imanity;

import java.util.HashSet;
import java.util.Set;

public class KeepChunkHandler {

    private Set<Long> chunksToKeep = new HashSet<>();

    public KeepChunkHandler() {
        Imanity.registerEvents(new Listener() {

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent event) {

                Chunk chunk = event.getChunk();
                if (!Imanity.SHUTTING_DOWN && isChunkToKeep(chunk.getX(), chunk.getZ())) {
                    event.setCancelled(true);
                }

            }

        });
    }

    public void addChunk(int x, int z) {
        this.chunksToKeep.add(LongHash.toLong(x, z));
    }

    public void removeChunk(int x, int z) {
        this.chunksToKeep.remove(LongHash.toLong(x, z));
    }

    public boolean isChunkToKeep(int x, int z) {
        return this.chunksToKeep.contains(LongHash.toLong(x, z));
    }

}
