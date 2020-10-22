/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.hologram.player.RenderedHolograms;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.metadata.MetadataKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HologramHandler {

    public static final int DISTANCE_TO_RENDER = 60;
    public static final MetadataKey<HologramHandler> WORLD_METADATA = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "WorldHolograms", HologramHandler.class);
    public static final MetadataKey<RenderedHolograms> HOLOGRAM_METADATA = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "Holograms", RenderedHolograms.class);
    private final Map<Integer, Hologram> holograms = new HashMap<>();

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
        Metadata.provideForPlayer(player).remove(HOLOGRAM_METADATA);

    }

    public Hologram getHologram(int id) {
        return this.holograms.get(id);
    }

    public Collection<Hologram> getHolograms() {
        return this.holograms.values();
    }

    public RenderedHolograms getRenderedHolograms(Player player) {
        return Metadata.provideForPlayer(player)
                .getOrPut(HOLOGRAM_METADATA, () -> new RenderedHolograms(player));
    }

    public void removeHologram(Hologram hologram) {
        this.holograms.remove(hologram.getId());
    }
}
