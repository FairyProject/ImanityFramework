package org.imanity.framework.chunk.block.location;

import net.minecraft.server.v1_8_R3.Chunk;

public interface YLocation {

    int get(int x, int z, Chunk chunk);

}
