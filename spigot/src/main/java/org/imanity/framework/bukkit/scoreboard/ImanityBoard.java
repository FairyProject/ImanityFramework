package org.imanity.framework.bukkit.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.packet.PacketService;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutScoreboardDisplayObjective;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutScoreboardObjective;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutScoreboardScore;
import org.imanity.framework.bukkit.packet.wrapper.server.WrappedPacketOutScoreboardTeam;
import org.imanity.framework.metadata.MetadataKey;
import org.imanity.framework.bukkit.util.BukkitUtil;

import java.util.List;

public class ImanityBoard {

    public static final MetadataKey<ImanityBoard> METADATA_TAG = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "Scoreboard", ImanityBoard.class);

    private final Player player;

    private String title;
    private final String[] teams;

    public ImanityBoard(Player player) {

        this.player = player;
        this.teams = new String[16];

        WrappedPacketOutScoreboardObjective packetA = new WrappedPacketOutScoreboardObjective(
                player.getName(),
                "Objective",
                WrappedPacketOutScoreboardObjective.HealthDisplayType.INTEGER,
                WrappedPacketOutScoreboardObjective.Action.ADD
        );

        WrappedPacketOutScoreboardDisplayObjective packetB = new WrappedPacketOutScoreboardDisplayObjective(
                DisplaySlot.SIDEBAR,
                player.getName()
        );

        PacketService.send(player, packetA);
        PacketService.send(player, packetB);

    }

    public void setTitle(String title) {

        if (this.title != null && this.title.equals(title)) {
            return;
        }

        this.title = title;

        PacketService.send(player, new WrappedPacketOutScoreboardObjective(
                player.getName(),
                title,
                WrappedPacketOutScoreboardObjective.HealthDisplayType.INTEGER,
                WrappedPacketOutScoreboardObjective.Action.CHANGED
        ));

    }

    public void setLines(List<String> lines) {

        int lineCount = 1;

        for (int i = lines.size() - 1; i >= 0; --i) {
            this.setLine(lineCount, BukkitUtil.color(lines.get(i)));

            lineCount++;
        }

        for (int i = lines.size(); i < 15; i++) {
            if (teams[lineCount] != null) {
                this.clear(lineCount);
            }

            lineCount++;
        }

    }

    private void setLine(int line, String value) {
        if (line <= 0 || line >= 16) {
            return;
        }

        if (teams[line] != null && teams[line].equals(value)) {
            return;
        }

        WrappedPacketOutScoreboardTeam packet = getOrRegisterTeam(line);
        String prefix;
        String suffix;

        if (value.length() <= 16) {
            prefix = value;
            suffix = "";
        } else {
            prefix = value.substring(0, 16);
            String lastColor = ChatColor.getLastColors(prefix);

            if (lastColor.isEmpty() || lastColor.equals(" "))
                lastColor = ChatColor.COLOR_CHAR + "f";

            if (prefix.endsWith(ChatColor.COLOR_CHAR + "")) {
                prefix = prefix.substring(0, 15);
                suffix = lastColor + value.substring(15);

            } else
                suffix = lastColor + value.substring(16);

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }
        }

        packet.setPrefix(prefix);
        packet.setSuffix(suffix);

        teams[line] = value;

        PacketService.send(player, packet);
    }

    public void clear(int line) {
        if (line > 0 && line < 16) {
            if (teams[line] != null) {

                WrappedPacketOutScoreboardScore packetA = new WrappedPacketOutScoreboardScore(
                        this.getEntry(line),
                        player.getName(),
                        line,
                        WrappedPacketOutScoreboardScore.ScoreboardAction.REMOVE
                );
                WrappedPacketOutScoreboardTeam packetB = getOrRegisterTeam(line);
                packetB.setAction(1);

                teams[line] = null;

                PacketService.send(player, packetA);
                PacketService.send(player, packetB);
            }
        }
    }

    public void remove() {
        for (int line = 1; line < 15; line++) {
            this.clear(line);
        }
    }

    private WrappedPacketOutScoreboardTeam getOrRegisterTeam(int line) {

        WrappedPacketOutScoreboardTeam packetB = WrappedPacketOutScoreboardTeam.builder()
                .name("-sb" + line)
                .action(0)
                .chatFormat(0)
                .build();

        if (teams[line] != null) {
            packetB.setAction(2);

            return packetB;
        } else {
            teams[line] = "";

            WrappedPacketOutScoreboardScore packetA = new WrappedPacketOutScoreboardScore(
                    this.getEntry(line),
                    player.getName(),
                    line,
                    WrappedPacketOutScoreboardScore.ScoreboardAction.CHANGE
            );

            packetB.setAction(0);
            packetB.getNameSet().add(getEntry(line));

            PacketService.send(player, packetA);

            return packetB;
        }
    }

    private String getEntry(Integer line) {
        if (line > 0 && line < 16)
            if (line <= 10)
                return ChatColor.COLOR_CHAR + "" + (line - 1) + ChatColor.WHITE;
            else {
                final String values = "a,b,c,d,e,f";
                final String[] next = values.split(",");

                return ChatColor.COLOR_CHAR + next[line - 11] + ChatColor.WHITE;
            }
        return "";
    }

}
