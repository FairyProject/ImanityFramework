package org.imanity.framework.bukkit.chunk.block.location;

import net.minecraft.server.v1_8_R3.Chunk;

public class YLocationHighest implements YLocation {

    @Override
    public int get(int x, int z, Chunk chunk) {
        return chunk.b(x & 15, z & 15);
    }

}
