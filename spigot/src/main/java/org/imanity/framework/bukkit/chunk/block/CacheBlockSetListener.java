package org.imanity.framework.bukkit.chunk.block;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.plugin.component.Component;

import static org.imanity.framework.bukkit.Imanity.PLUGIN;

@Component
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
