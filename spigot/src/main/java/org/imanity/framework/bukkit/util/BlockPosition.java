package org.imanity.framework.bukkit.util;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BlockPosition {

    private int x;
    private int y;
    private int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition() {
        this(0 , 0, 0);
    }

}
