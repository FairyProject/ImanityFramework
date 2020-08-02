package org.imanity.framework.bukkit.chunk.block.location;

import net.minecraft.server.v1_8_R3.Chunk;

public interface YLocation {

    int get(int x, int z, Chunk chunk);

}
