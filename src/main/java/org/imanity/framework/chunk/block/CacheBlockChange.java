package org.imanity.framework.chunk.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.imanity.framework.chunk.block.location.YLocation;

@AllArgsConstructor
@Getter
@Setter
public class CacheBlockChange {

    private int x;
    private YLocation y;
    private int z;
    private Material material;
    private byte data;

}
