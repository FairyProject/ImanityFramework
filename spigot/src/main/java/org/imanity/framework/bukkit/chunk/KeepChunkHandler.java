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

package org.imanity.framework.bukkit.chunk;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.listener.events.EventSubscription;
import org.imanity.framework.bukkit.listener.events.Events;
import org.imanity.framework.plugin.service.IService;
import org.imanity.framework.plugin.service.Service;

import java.util.HashSet;
import java.util.Set;

@Service(name = "keepChunk")
public class KeepChunkHandler implements IService {

    private Set<Long> chunksToKeep;
    private EventSubscription<ChunkUnloadEvent> eventSubscription;

    @Override
    public void init() {
        this.chunksToKeep = new HashSet<>();

        this.eventSubscription = Events.subscribe(ChunkUnloadEvent.class)
                .listen((sub, event) -> {
                    Chunk chunk = event.getChunk();
                    if (!Imanity.SHUTTING_DOWN && isChunkToKeep(chunk.getX(), chunk.getZ())) {
                        event.setCancelled(true);
                    }
                }).build(Imanity.PLUGIN);
    }

    @Override
    public void stop() {
        this.eventSubscription.unregister();
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
