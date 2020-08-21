package org.imanity.framework.bukkit.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class FormatUtil {

    public static String formatToSecondsAndMinutes(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String formatToMinutesAndHours(int seconds) {
        return String.format("%02d:%02d", seconds / 3600, seconds % 3600 / 60);
    }

    public static String formatMillisToMinutesAndHours(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        return String.format("%02d:%02d", seconds / 3600, seconds % 3600 / 60);
    }

    public static String formatMillis(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (seconds >= 3600) {
            return String.format("%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String formatSeconds(int seconds) {
        if (seconds >= 3600) {
            return String.format("%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static String getBooleanSymbol(boolean bol) {
        return bol ? ChatColor.GREEN + StringEscapeUtils.unescapeJava("\u221a") : ChatColor.RED + "X";
    }

    public static String formatTimes(long millis) {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millis);
        return seconds >= 3600 ? (seconds / 3600) + "h" : seconds >= 60 ? (seconds / 60 + 1) + "m" : seconds + "s";
    }

}
