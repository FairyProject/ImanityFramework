package org.imanity.framework.bukkit.hologram.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.hologram.Hologram;
import org.imanity.framework.bukkit.hologram.HologramHandler;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RenderedHolograms {

    private String worldName;
    private final List<Integer> holograms = new ArrayList<>();

    public RenderedHolograms(Player player) {
        this.worldName = player.getWorld().getName();
    }

    public void removeFarHolograms(Player player, HologramHandler hologramHandler) {

        String newWorldName = player.getWorld().getName();
        if (this.worldName.equals(newWorldName)) {

            this.reset(player, hologramHandler);
            this.worldName = newWorldName;

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
