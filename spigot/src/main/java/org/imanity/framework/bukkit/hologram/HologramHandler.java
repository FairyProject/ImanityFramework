package org.imanity.framework.bukkit.hologram;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.hologram.player.RenderedHolograms;
import org.imanity.framework.bukkit.util.SampleMetadata;

import java.util.Collection;

public class HologramHandler {

    public static final int DISTANCE_TO_RENDER = 60;
    public static final String WORLD_METADATA = ImanityCommon.METADATA_PREFIX + "WorldHolograms";
    public static final String HOLOGRAM_METADATA = ImanityCommon.METADATA_PREFIX + "Holograms";
    private Int2ObjectMap<Hologram> holograms = new Int2ObjectOpenHashMap<>();

    public Hologram addHologram(Location location, String... texts) {
        Hologram hologram = new Hologram(location, this);
        for (String text : texts) {
            hologram.addText(text);
        }
        this.addHologram(hologram);
        hologram.spawn();
        return hologram;
    }

    public void addHologram(Hologram hologram) {
        this.holograms.put(hologram.getId(), hologram);
    }

    public void update(Player player) {

        RenderedHolograms holograms = this.getRenderedHolograms(player);
        holograms.removeFarHolograms(player, this);
        holograms.addNearHolograms(player, this);

    }

    public void reset(Player player) {

        RenderedHolograms holograms = this.getRenderedHolograms(player);
        holograms.reset(player, this);

        player.removeMetadata(HOLOGRAM_METADATA, Imanity.PLUGIN);

    }

    public Hologram getHologram(int id) {
        return this.holograms.get(id);
    }

    public Collection<Hologram> getHolograms() {
        return this.holograms.values();
    }

    public RenderedHolograms getRenderedHolograms(Player player) {

        if (player.hasMetadata(HOLOGRAM_METADATA)) {
            return (RenderedHolograms) player.getMetadata(HOLOGRAM_METADATA).get(0).value();
        }

        RenderedHolograms holograms = new RenderedHolograms(player);
        player.setMetadata(HOLOGRAM_METADATA, new SampleMetadata(holograms));

        return holograms;
    }

}
