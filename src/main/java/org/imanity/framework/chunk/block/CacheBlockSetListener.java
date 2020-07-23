package org.imanity.framework.chunk.block;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.imanity.framework.Imanity;
import org.imanity.framework.util.SampleMetadata;

import static org.imanity.framework.Imanity.PLUGIN;

public class CacheBlockSetListener implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkPopulateEvent event) {
        Chunk chunk = event.getChunk();

        CacheBlockSetHandler blockSetHandler = Imanity.getBlockSetHandler(event.getWorld());
        if (blockSetHandler != null) {
            blockSetHandler.placeIfExists(chunk);
        }
    }

    @EventHandler
    public void onWorldLoad(WorldInitEvent event) {
        CacheBlockSetHandler blockSetHandler = new CacheBlockSetHandler(event.getWorld());
        event.getWorld().setMetadata(CacheBlockSetHandler.METADATA, new SampleMetadata(blockSetHandler));
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        event.getWorld().removeMetadata(CacheBlockSetHandler.METADATA, PLUGIN);
    }

}
