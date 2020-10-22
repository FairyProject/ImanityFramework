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
