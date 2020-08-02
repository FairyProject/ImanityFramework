package org.imanity.framework.bukkit.chunk.block.location;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.Chunk;

@RequiredArgsConstructor
public class YLocationFixed implements YLocation {

    private final int y;

    @Override
    public int get(int x, int z, Chunk chunk) {
        return y;
    }
}
