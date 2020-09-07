package org.imanity.framework.bukkit.tablist.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class LegacyClientUtil {

    private static final String[] TAB_ENTRIES;
    private static final String[] TEAM_NAMES;

    static {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            String entry = ChatColor.values()[i].toString();
            list.add(ChatColor.RED + entry);
            list.add(ChatColor.GREEN + entry);
            list.add(ChatColor.DARK_RED + entry);
            list.add(ChatColor.DARK_GREEN + entry);
            list.add(ChatColor.BLUE + entry);
            list.add(ChatColor.DARK_BLUE + entry);
        }
        TAB_ENTRIES = list.toArray(new String[0]);

        list = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            String s = (i < 10 ? "\\u00010" : "\\u0001") + i;
            list.add(s);
        }
        TEAM_NAMES = list.toArray(new String[0]);
    }

    public static String entry(int rawSlot) {
        return TAB_ENTRIES[rawSlot];
    }

    public static String name(int rawSlot) {
        return TEAM_NAMES[rawSlot];
    }
}
