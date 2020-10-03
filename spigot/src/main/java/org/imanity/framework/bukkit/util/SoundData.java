package org.imanity.framework.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
public class SoundData {

    private Sound sound;
    private float volume;
    private float pitch;

    public SoundData(Sound sound) {
        this(sound, 0.0F, 0.0F);
    }

    public void play(Player... players) {
        for (Player player : players) {
            player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
        }
    }

    public void play(Iterable<Player> players) {
        for (Player player : players) {
            player.playSound(player.getLocation(), this.sound, this.volume, this.pitch);
        }
    }

    public void play(Location location) {
        location.getWorld().playSound(location, this.sound, this.volume, this.pitch);
    }

    public void play(Location location, Player... players) {
        for (Player player : players) {
            player.playSound(location, this.sound, this.volume, this.pitch);
        }
    }

    public void play(Location location, Iterable<Player> players) {
        for (Player player : players) {
            player.playSound(location, this.sound, this.volume, this.pitch);
        }
    }

    public static SoundData of(Sound sound) {
        return new SoundData(sound);
    }

    public static SoundData of(Sound sound, float volume, float pitch) {
        return new SoundData(sound, volume, pitch);
    }

    public static SoundData ofVolume(Sound sound, float volume) {
        return new SoundData(sound, volume, 0.0F);
    }

    public static SoundData ofPitch(Sound sound, float pitch) {
        return new SoundData(sound, 0.0F, pitch);
    }

}
