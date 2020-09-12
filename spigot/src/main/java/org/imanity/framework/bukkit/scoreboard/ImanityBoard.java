package org.imanity.framework.bukkit.scoreboard;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.metadata.MetadataKey;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.EnumWrapper;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.PacketWrapper;

import java.util.List;

public class ImanityBoard {

    private static EnumWrapper SCOREBOARD_HEALTH_DISPLAY;

    private static EnumWrapper SCOREBOARD_ACTION;

    private static final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

    static {
        try {
            Class<?> enumScoreboardHealthDisplay = CLASS_RESOLVER.resolveSilent("IScoreboardCriteria$EnumScoreboardHealthDisplay");
            SCOREBOARD_HEALTH_DISPLAY = new EnumWrapper(enumScoreboardHealthDisplay);

            Class<?> TYPE_SCOREBOARD_ACTION = CLASS_RESOLVER.resolveSilent("PacketPlayOutScoreboardScore$EnumScoreboardAction");
            SCOREBOARD_ACTION = new EnumWrapper(TYPE_SCOREBOARD_ACTION);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static final MetadataKey<ImanityBoard> METADATA_TAG = MetadataKey.create("Imanity-Scoreboard", ImanityBoard.class);

    private final Player player;

    private String title;
    private final String[] teams;

    @Getter
    @Setter
    private boolean tagUpdated;

    public ImanityBoard(Player player) {

        this.player = player;
        this.teams = new String[16];

        PacketWrapper packetA = PacketWrapper.createByPacketName("PacketPlayOutScoreboardObjective")
                .setPacketValue("b", "Objective")
                .setPacketValue("a", player.getName())
                .setPacketValue("c", SCOREBOARD_HEALTH_DISPLAY.getType("INTEGER"))
                .setPacketValue("d", 0);

        PacketWrapper packetB = PacketWrapper.createByPacketName("PacketPlayOutScoreboardDisplayObjective")
                .setPacketValue("a", 1)
                .setPacketValue("b", player.getName());

        MinecraftReflection.sendPacket(player, packetA);
        MinecraftReflection.sendPacket(player, packetB);

    }

    public void setTitle(String title) {

        if (this.title != null && this.title.equals(title)) {
            return;
        }

        this.title = title;

        MinecraftReflection.sendPacket(player, PacketWrapper.createByPacketName("PacketPlayOutScoreboardObjective")
        .setPacketValue("a", player.getName())
        .setPacketValue("b", title)
        .setPacketValue("c", SCOREBOARD_HEALTH_DISPLAY.getType("INTEGER"))
        .setPacketValue("d", 2));

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

        PacketWrapper packet = getOrRegisterTeam(line);
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

        packet.setPacketValue("c", prefix);
        packet.setPacketValue("d", suffix);

        teams[line] = value;

        MinecraftReflection.sendPacket(player, packet);
    }

    public void clear(int line) {
        if (line > 0 && line < 16) {
            if (teams[line] != null) {

                PacketWrapper packetA = PacketWrapper.createByPacketName("PacketPlayOutScoreboardScore")
                        .setPacketValue("a", this.getEntry(line))
                        .setPacketValue("b", player.getName())
                        .setPacketValue("c", line)
                        .setPacketValue("d", SCOREBOARD_ACTION.getType("REMOVE"));
                PacketWrapper packetB = getOrRegisterTeam(line)
                        .setPacketValue("h", 1);

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

    private PacketWrapper getOrRegisterTeam(int line) {
        if (line <= 0 || line >= 16)
            return null;

        PacketWrapper packetB = PacketWrapper.createByPacketName("PacketPlayOutScoreboardTeam")
                .setPacketValue("a", "-sb" + line)
                .setPacketValue("b", "")
                .setPacketValue("c", "")
                .setPacketValue("d", "")
                .setPacketValue("i", 0)
                .setPacketValue("e", "always")
                .setPacketValue("f", 0);

        if (teams[line] != null) {
            packetB.setPacketValue("h", 2);

            return packetB;
        } else {
            teams[line] = "";

            PacketWrapper packetA = PacketWrapper.createByPacketName("PacketPlayOutScoreboardScore")
                    .setPacketValue("a", getEntry(line))
                    .setPacketValue("b", player.getName())
                    .setPacketValue("c", line)
                    .setPacketValue("d", SCOREBOARD_ACTION.getType("CHANGE"));

            packetB.setPacketValue("h", 0);
            ((List<String>) packetB.getPacketValue("g")).add(getEntry(line));

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
