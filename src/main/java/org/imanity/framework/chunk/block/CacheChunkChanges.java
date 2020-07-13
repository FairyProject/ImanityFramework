package org.imanity.framework.chunk.block;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;

import java.util.ArrayList;
import java.util.List;

public class CacheChunkChanges {

    private List<CacheBlockChange> blockChanges = new ArrayList<>();

    public void add(CacheBlockChange blockChange) {
        this.blockChanges.add(blockChange);
    }

    public void place(Chunk chunk) {

        BlockPosition.MutableBlockPosition blockPosition = new BlockPosition.MutableBlockPosition();
        for (CacheBlockChange blockChange : this.blockChanges) {

            final int combined = blockChange.getMaterial().getId() + (blockChange.getData() << 12);
            final IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(combined);

            int y = blockChange.getY().get(blockChange.getX(), blockChange.getZ(), chunk);
            chunk.a(blockPosition.c(blockChange.getX(), y, blockChange.getZ()), ibd);

            chunk.world.notify(blockPosition);
        }

    }

    public void free() {
        blockChanges.clear();
        blockChanges = null;
    }

}
