package org.imanity.framework.bukkit.util.items;

import lombok.Getter;
import org.bukkit.Material;

import javax.annotation.Nullable;

public enum ArmorPart {

    HELMET(0),
    CHESTPLATE(1),
    LEGGINGS(2),
    BOOTS(3);

    @Getter
    private int slot;

    ArmorPart(int slot) {
        this.slot = slot;
    }

    @Nullable
    public ArmorPart getByType(Material material) {
        for (ArmorPart part : ArmorPart.values()) {
            if (material.name().contains(part.name())) {
                return part;
            }
        }

        return null;
    }

}
