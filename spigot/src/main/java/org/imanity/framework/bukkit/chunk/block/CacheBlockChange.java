package org.imanity.framework.bukkit.chunk.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.imanity.framework.bukkit.chunk.block.location.YLocation;

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
