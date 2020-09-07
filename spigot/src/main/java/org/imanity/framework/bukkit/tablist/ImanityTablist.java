package org.imanity.framework.bukkit.tablist;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.tablist.util.*;
import org.imanity.framework.bukkit.util.TaskUtil;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.version.PlayerVersion;

import java.util.*;

@Getter
public class ImanityTablist {

    private final Player player;
    private final Set<TabEntry> currentEntries = new HashSet<>();

    private String header;
    private String footer;

    public ImanityTablist(Player player) {
        this.player = player;
        this.setup();
        Team team1 = player.getScoreboard().getTeam("\\u000181");
        if (team1 == null) {
            team1 = player.getScoreboard().registerNewTeam("\\u000181");
        }
        team1.addEntry(player.getName());
        for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
            Team team = loopPlayer.getScoreboard().getTeam("\\u000181");
            if (team == null) {
                team = loopPlayer.getScoreboard().registerNewTeam("\\u000181");
            }
            team.addEntry(player.getName());
            team.addEntry(loopPlayer.getName());
            team1.addEntry(loopPlayer.getName());
            team1.addEntry(player.getName());
        }

        if (MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7) {
            TaskUtil.runScheduled(() -> ImanityTabHandler.getInstance().getImplementation().removeSelf(player), 1L);
        }
    }

    private void setup() {
        final int possibleSlots = MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7 ? 60 : 80;
        for (int i = 1; i <= possibleSlots; i++) {
            final TabColumn tabColumn = TabColumn.getFromSlot(player, i);
            if (tabColumn == null) {
                continue;
            }

            TabEntry tabEntry = ImanityTabHandler.getInstance().getImplementation().createFakePlayer(
                    this,
                    "0" + (i > 9 ? i : "0" + i) + "|Tab",
                    tabColumn,
                    tabColumn.getNumb(player, i),
                    i
            );
            if (MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7) {
                Team team = player.getScoreboard().getTeam(LegacyClientUtil.name(i - 1));
                if (team != null) {
                    team.unregister();
                }
                team = player.getScoreboard().registerNewTeam(LegacyClientUtil.name(i - 1));
                team.setPrefix("");
                team.setSuffix("");
                team.addEntry(LegacyClientUtil.entry(i - 1));
            }
            currentEntries.add(tabEntry);
        }
    }

    public void update() {
        ImanityTabAdapter adapter = ImanityTabHandler.getInstance().getAdapter();

        Set<TabEntry> previous = new HashSet<>(currentEntries);

        Set<BufferedTabObject> processedObjects = adapter.getSlots(player);
        if (processedObjects == null) {
            processedObjects = new HashSet<>();
        }

        for (BufferedTabObject scoreObject : processedObjects) {
            TabEntry tabEntry = getEntry(scoreObject.getColumn(), scoreObject.getSlot());
            if (tabEntry != null) {
                previous.remove(tabEntry);
                if (scoreObject.getPing() == null) {
                    ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, 0);
                } else {
                    ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, scoreObject.getPing());
                }

                ImanityTabHandler.getInstance().getImplementation().updateFakeName(this, tabEntry, scoreObject.getText());
                if (MinecraftReflection.getProtocol(player) != PlayerVersion.v1_7) {
                    if (!tabEntry.getTexture().toString().equals(scoreObject.getTabIcon().toString())) {
                        ImanityTabHandler.getInstance().getImplementation().updateFakeSkin(this, tabEntry, scoreObject.getTabIcon());
                    }
                }
            }
        }
        for (TabEntry tabEntry : previous) {
            ImanityTabHandler.getInstance().getImplementation().updateFakeName(this, tabEntry, "");
            ImanityTabHandler.getInstance().getImplementation().updateFakeLatency(this, tabEntry, 0);
            if (MinecraftReflection.getProtocol(player) != PlayerVersion.v1_7) {
                ImanityTabHandler.getInstance().getImplementation().updateFakeSkin(this, tabEntry, TabIcon.GRAY);
            }
        }
        previous.clear();

        String headerNow = Utility.color(adapter.getHeader(player));
        String footerNow = Utility.color(adapter.getFooter(player));

        if (!headerNow.equals(this.header) || !footerNow.equals(this.footer)) {
            ImanityTabHandler.getInstance().getImplementation().updateHeaderAndFooter(this, headerNow, footerNow);
            this.header = headerNow;
            this.footer = footerNow;
        }
    }

    public TabEntry getEntry(TabColumn column, Integer slot){
        for (TabEntry entry : currentEntries){
            if (entry.getColumn().name().equalsIgnoreCase(column.name()) && entry.getSlot() == slot){
                return entry;
            }
        }
        return null;
    }

    public static String[] splitStrings(String text, int rawSlot) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == ChatColor.COLOR_CHAR || prefix.charAt(15) == '&') {
                prefix = prefix.substring(0, 15);
                suffix = text.substring(15);
            } else if (prefix.charAt(14) == ChatColor.COLOR_CHAR || prefix.charAt(14) == '&') {
                prefix = prefix.substring(0, 14);
                suffix = text.substring(14);
            } else {
                suffix = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&',prefix)) + text.substring(16, text.length());
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            //Bukkit.broadcastMessage(prefix + " |||| " + suffix);
            return new String[] {
                    prefix,
                    suffix
            };
        } else {
            return new String[] {
                    text
            };
        }
    }
}
