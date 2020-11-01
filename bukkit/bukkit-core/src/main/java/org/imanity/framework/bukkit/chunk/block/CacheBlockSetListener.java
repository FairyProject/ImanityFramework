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

package org.imanity.framework.bukkit.chunk.block;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.metadata.Metadata;
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
        Metadata.provideForWorld(event.getWorld()).put(CacheBlockSetHandler.METADATA, blockSetHandler);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        Metadata.provideForWorld(event.getWorld()).remove(CacheBlockSetHandler.METADATA);
    }

}
