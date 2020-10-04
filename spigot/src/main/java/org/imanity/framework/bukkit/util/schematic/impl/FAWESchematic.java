package org.imanity.framework.bukkit.util.schematic.impl;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.imanity.framework.bukkit.util.BlockPosition;

import java.io.File;
import java.io.IOException;

public class FAWESchematic extends WorldEditSchematic {

    public FAWESchematic(File file) {
        super(file);
    }

    public FAWESchematic(File file, BlockPosition top, BlockPosition bottom) {
        super(file, top, bottom);
    }

    @Override
    public void save(org.bukkit.World world) throws IOException {
        Preconditions.checkNotNull(this.file);
        Preconditions.checkNotNull(this.top);
        Preconditions.checkNotNull(this.bottom);

        Vector top = new Vector(this.top.getX(), this.top.getY(), this.top.getZ());
        Vector bottom = new Vector(this.bottom.getX(), this.bottom.getY(), this.bottom.getZ());

        CuboidRegion region = new CuboidRegion(new BukkitWorld(world), top, bottom);
        com.boydti.fawe.object.schematic.Schematic schematic = new com.boydti.fawe.object.schematic.Schematic(region);
        schematic.save(this.file, ClipboardFormat.SCHEMATIC);
    }
}
