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
