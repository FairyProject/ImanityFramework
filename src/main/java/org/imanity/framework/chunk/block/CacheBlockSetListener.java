package org.imanity.framework.chunk.block;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.imanity.framework.Imanity;

public class CacheBlockSetListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkPopulateEvent event) {
        Chunk chunk = event.getChunk();

        CacheBlockSetHandler blockSetHandler = Imanity.getBlockSetHandler(event.getWorld());
        if (blockSetHandler != null) {
            blockSetHandler.placeIfExists(chunk);
        }
    }

}
