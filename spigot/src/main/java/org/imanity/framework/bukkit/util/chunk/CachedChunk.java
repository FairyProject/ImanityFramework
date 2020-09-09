package org.imanity.framework.bukkit.util.chunk;

import org.bukkit.ChunkSnapshot;
import org.bukkit.material.MaterialData;

public interface CachedChunk extends ChunkSnapshot {

    MaterialData getBlock(int x, int y, int z);

    void setBlock(int var1, int var2, int var3, MaterialData materialData);

}
