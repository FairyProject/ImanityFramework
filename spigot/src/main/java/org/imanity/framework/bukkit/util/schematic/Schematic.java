package org.imanity.framework.bukkit.util.schematic;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Location;
import org.bukkit.World;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.schematic.impl.FAWESchematic;
import org.imanity.framework.bukkit.util.schematic.impl.WorldEditSchematic;

import java.io.File;
import java.io.IOException;

@Getter
@Setter
public abstract class Schematic {

    private static final SchematicType TYPE;

    public static Schematic create(File file) {
        switch (Schematic.TYPE) {
            case FAWE:
                return new FAWESchematic(file);
            case WORLDEDIT:
                return new WorldEditSchematic(file);
        }

        throw new UnsupportedOperationException("Couldn't find SchematicType!");
    }

    public static Schematic create(File file, BlockPosition top, BlockPosition bottom) {
        switch (Schematic.TYPE) {
            case FAWE:
                return new FAWESchematic(file, top, bottom);
            case WORLDEDIT:
                return new WorldEditSchematic(file, top, bottom);
        }

        throw new UnsupportedOperationException("Couldn't find SchematicType!");
    }

    static {
        SchematicType lookupType = SchematicType.BUKKIT;

        lookup:
        {
            try {
                Class.forName("com.boydti.fawe.FaweAPI");
                lookupType = SchematicType.FAWE;
                break lookup;
            } catch (ClassNotFoundException ex) {
            }

            try {
                Class.forName("com.sk89q.worldedit.EditSession");
                lookupType = SchematicType.WORLDEDIT;
                break lookup;
            } catch (ClassNotFoundException ex) {
            }

        }

        TYPE = lookupType;
    }

    protected File file;
    protected BlockPosition top;
    protected BlockPosition bottom;

    public Schematic(File file) {
        this.file = file;
    }

    public Schematic(File file, BlockPosition top, BlockPosition bottom) {
        this.file = file;
        this.top = top;
        this.bottom = bottom;
    }

    public abstract void save(World world) throws IOException;

    public abstract void paste(Location location, int rotateX, int rotateY, int rotateZ) throws IOException;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("file", file)
                .append("top", top)
                .append("bottom", bottom)
                .toString();
    }
}
