package org.imanity.framework.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;

public class SpigotUtil {

    public static SpigotType SPIGOT_TYPE;

    public static void init() {

        try {
            Class.forName("spg.lgdev.config.iSpigotConfig");
            SPIGOT_TYPE = SpigotType.IMANITY;
            return;
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("org.github.paperspigot.PaperSpigotConfig");
            SPIGOT_TYPE = SpigotType.PAPER;
            return;
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            SPIGOT_TYPE = SpigotType.PAPER;
            return;
        } catch (ClassNotFoundException e) {}

        try {
            Class.forName("org.spigotmc.SpigotConfig");
            SPIGOT_TYPE = SpigotType.SPIGOT;
            return;
        } catch (ClassNotFoundException e) {}

        SPIGOT_TYPE = SpigotType.CRAFTBUKKIT;

    }

    public static boolean isServerThread() {
        if (SPIGOT_TYPE == SpigotType.IMANITY) {
            return Bukkit.isPrimaryThread(false);
        }
        return Bukkit.isPrimaryThread();
    }

    public static int getProtocolVersion(Player player) {
        return MinecraftReflection.getProtocol(player).getRawVersion()[0];
    }

    public enum SpigotType {

        IMANITY,
        PAPER,
        SPIGOT,
        CRAFTBUKKIT

    }

}
