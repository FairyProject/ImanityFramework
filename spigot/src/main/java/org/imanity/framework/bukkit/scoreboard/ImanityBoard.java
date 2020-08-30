package org.imanity.framework.bukkit.scoreboard;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImanityBoard {

    private static Field PACKET_G_FIELD;

    static {
        try {
            PACKET_G_FIELD = PacketPlayOutScoreboardTeam.class.getDeclaredField("g");
            PACKET_G_FIELD.setAccessible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static final String METADATA_TAG = "Imanity-Scoreboard";

    private final Player player;
    private final Map<String, ImanityTeamData> teamDatas = new HashMap<>();

    private String title;
    private final String[] teams;

    private final ImanityBoardAdapter adapter;

    @Getter
    @Setter
    private boolean tagUpdated;

    public ImanityBoard(Player player, ImanityBoardAdapter adapter) {

        this.player = player;
        this.teams = new String[16];
        this.adapter = adapter;

        PacketPlayOutScoreboardObjective packetA = new PacketPlayOutScoreboardObjective();

        packetA.setB("Objective");
        packetA.setA(player.getName());
        packetA.setC(IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        packetA.setD(0);

        PacketPlayOutScoreboardDisplayObjective packetB = new PacketPlayOutScoreboardDisplayObjective();

        packetB.setA(1);
        packetB.setB(player.getName());

        MinecraftReflection.sendPacket(player, packetA);
        MinecraftReflection.sendPacket(player, packetB);

    }

    public void registerTeam(ImanityTeamData data) {
        this.teamDatas.put(data.getName(), data);

        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

        packet.setE(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e);
        packet.setF(-1);
        packet.setG(data.getNameSet());
        packet.setA(data.getName());
        packet.setH(0);
        packet.setB(data.getName());
        packet.setC(data.getPrefix());
        packet.setD("");


        int d = 0;

        if (data.isFriendlyFire()) {
            d |= 1;
        }
        if (data.isFriendlyInvisibles()) {
            d |= 2;
        }

        packet.setI(d);

        MinecraftReflection.sendPacket(player, packet);
    }

    public void updatePlayer(Player target) {
        String name = this.adapter.getTeam(player, target);

        if (name == null) {
            return;
        }

        ImanityTeamData data = this.teamDatas.get(name);
        data.addName(target.getName());

        this.addToTeam(data, target.getName());
    }

    public void removePlayer(Player target) {

        for (ImanityTeamData team : this.teamDatas.values()) {

            if (team.getNameSet().contains(target.getName())) {

                team.removeName(target.getName());
                this.removeToTeam(team, target.getName());

            }

        }

    }

    public void addToTeam(ImanityTeamData data, String name) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        packet.setE(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e);
        packet.setF(-1);
        packet.setG(Collections.singleton(name));
        packet.setH(3);
        packet.setA(data.getName());
        MinecraftReflection.sendPacket(player, packet);
    }

    public void removeToTeam(ImanityTeamData data, String name) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        packet.setE(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS.e);
        packet.setF(-1);
        packet.setG(Collections.singleton(name));
        packet.setH(4);
        packet.setA(data.getName());
        MinecraftReflection.sendPacket(player, packet);
    }

    public void setTitle(String title) {

        if (this.title != null && this.title.equals(title)) {
            return;
        }

        this.title = title;

        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();
        packet.setA(player.getName());
        packet.setB(title);
        packet.setC(IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        packet.setD(2);

        MinecraftReflection.sendPacket(player, packet);

    }

    public void setLines(List<String> lines) {

        int lineCount = 1;

        for (int i = lines.size() - 1; i >= 0; --i) {
            this.setLine(lineCount, Utility.color(lines.get(i)));

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

        PacketPlayOutScoreboardTeam packet = getOrRegisterTeam(line);
        String prefix = "";
        String suffix = "";

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

        packet.setC(prefix);
        packet.setD(suffix);

        teams[line] = value;

        MinecraftReflection.sendPacket(player, packet);
    }

    public void clear(int line) {
        if (line > 0 && line < 16) {
            if (teams[line] != null) {

                PacketPlayOutScoreboardScore packetA = new PacketPlayOutScoreboardScore(this.getEntry(line));
                PacketPlayOutScoreboardTeam packetB = getOrRegisterTeam(line);

                packetB.setH(1);
                packetA.setB(player.getName());
                packetA.setC(line);
                packetA.setD(PacketPlayOutScoreboardScore.EnumScoreboardAction.REMOVE);

                teams[line] = null;

                MinecraftReflection.sendPacket(player, packetA);
                MinecraftReflection.sendPacket(player, packetB);
            }
        }
    }

    public void remove() {
        for (int line = 1; line < 15; line++) {
            this.clear(line);
        }
    }

    private PacketPlayOutScoreboardTeam getOrRegisterTeam(int line) {
        if (line <= 0 || line >= 16)
            return null;

        if (teams[line] != null) {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

            packet.setA("-sb" + line);
            packet.setB("");
            packet.setC("");
            packet.setD("");
            packet.setI(0);
            packet.setE("always");
            packet.setF(0);
            packet.setH(2);

            return packet;
        } else {
            teams[line] = "";

            PacketPlayOutScoreboardScore packetA = new PacketPlayOutScoreboardScore();

            packetA.setA(getEntry(line));
            packetA.setB(player.getName());
            packetA.setC(line);
            packetA.setD(PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE);

            PacketPlayOutScoreboardTeam packetB = new PacketPlayOutScoreboardTeam();

            packetB.setA("-sb" + line);
            packetB.setB("");
            packetB.setC("");
            packetB.setD("");
            packetB.setI(0);
            packetB.setE("always");
            packetB.setF(0);
            packetB.setH(0);

            try {
                ((List<String>) PACKET_G_FIELD.get(packetB)).add(getEntry(line));
            } catch (IllegalAccessException e) {
                Utility.error(e, "An error occurred while creating new fake team");
            }

            MinecraftReflection.sendPacket(player, packetA);

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
