package org.imanity.framework.bukkit.hologram.player;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.Hologram;
import org.imanity.framework.bukkit.hologram.HologramHandler;
import org.imanity.framework.bukkit.util.SpigotUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RenderedHolograms {

    private int worldId;
    private final ArrayList<Integer> holograms = new ArrayList<>();

    public RenderedHolograms(Player player) {
        this.worldId = SpigotUtil.getWorldId(player.getWorld());
    }

    public void removeFarHolograms(Player player, HologramHandler hologramHandler) {

        int newWorldId = SpigotUtil.getWorldId(player.getWorld());
        if (this.worldId != newWorldId) {

            this.reset(player, hologramHandler);
            this.worldId = newWorldId;

            return;
        }

        this.holograms.removeIf(id -> {
            Hologram hologram = hologramHandler.getHologram(id);

            if (hologram == null) {
                return true;
            }

            if (hologram.distaneTo(player) > HologramHandler.DISTANCE_TO_RENDER) {
                hologram.removePlayer(player);
                return true;
            }

            return false;
        });
    }

    public void removeHologram(Player player, Hologram hologram) {
        hologram.removePlayer(player);
        this.holograms.remove((Integer) hologram.getId());
    }

    public void reset(Player player, HologramHandler hologramHandler) {
        this.holograms.forEach(id -> {
            Hologram hologram = hologramHandler.getHologram(id);
            if (hologram == null) {
                return;
            }

            hologram.removePlayer(player);
        });
        this.holograms.clear();
    }

    public void addNearHolograms(Player player, HologramHandler hologramHandler) {
        hologramHandler.getHolograms()
                .stream()
                .filter(hologram -> !this.holograms.contains(hologram.getId()))
                .filter(hologram -> hologram.distaneTo(player) <= HologramHandler.DISTANCE_TO_RENDER)
                .forEach(hologram -> {
                    hologram.spawnPlayer(player);
                    this.holograms.add(hologram.getId());
                });
    }

}
