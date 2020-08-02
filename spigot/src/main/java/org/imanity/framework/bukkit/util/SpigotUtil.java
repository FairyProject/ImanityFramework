package org.imanity.framework.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

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

    public static int getWorldId(World world) {
        return ((CraftWorld) world).getHandle().dimension;
    }

    public static boolean isServerThread() {
        if (SPIGOT_TYPE == SpigotType.IMANITY) {
            return Bukkit.isPrimaryThread(false);
        }
        return Bukkit.isPrimaryThread();
    }

    public static int getProtocolVersion(Player player) {
        if (SPIGOT_TYPE == SpigotType.IMANITY) {
            return ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion();
        }
        return -1;
    }

    public enum SpigotType {

        IMANITY,
        PAPER,
        SPIGOT,
        CRAFTBUKKIT

    }

}
