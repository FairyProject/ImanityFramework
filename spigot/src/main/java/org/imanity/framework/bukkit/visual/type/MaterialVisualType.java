package org.imanity.framework.bukkit.visual.type;

import lombok.Builder;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.visual.VisualBlockData;

@Getter
@Builder
public class MaterialVisualType extends VisualType {

    private final Material material;
    private final byte data;

    @Override
    public VisualBlockData generate(Player player, BlockPosition blockPosition) {
        return new VisualBlockData(material, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialVisualType that = (MaterialVisualType) o;

        if (data != that.data) return false;
        return material == that.material;
    }

    @Override
    public int hashCode() {
        int result = material != null ? material.hashCode() : 0;
        result = 31 * result + (int) data;
        return result;
    }
}
