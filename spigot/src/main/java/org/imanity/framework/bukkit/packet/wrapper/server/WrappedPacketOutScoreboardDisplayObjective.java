package org.imanity.framework.bukkit.packet.wrapper.server;

import com.google.common.collect.ImmutableBiMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scoreboard.DisplaySlot;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.SendableWrapper;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.wrapper.PacketWrapper;

@AutowiredWrappedPacket(value = PacketType.Server.SCOREBOARD_DISPLAY_OBJECTIVE, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutScoreboardDisplayObjective extends WrappedPacket implements SendableWrapper {

    private static ImmutableBiMap<DisplaySlot, Integer> DISPLAY_SLOT_TO_ID;

    public static void init() {

        DISPLAY_SLOT_TO_ID = ImmutableBiMap.<DisplaySlot, Integer>builder()
                .put(DisplaySlot.PLAYER_LIST, 0)
                .put(DisplaySlot.SIDEBAR, 1)
                .put(DisplaySlot.BELOW_NAME, 2)
                .build();

    }

    private DisplaySlot displaySlot;
    private String objective;

    public WrappedPacketOutScoreboardDisplayObjective(Object packet) {
        super(packet);
    }

    public WrappedPacketOutScoreboardDisplayObjective(DisplaySlot displaySlot, String objective) {
        this.displaySlot = displaySlot;
        this.objective = objective;
    }

    @Override
    protected void setup() {
        this.displaySlot = DISPLAY_SLOT_TO_ID.inverse().get(readInt(0));
        this.objective = readString(0);
    }

    @Override
    public Object asNMSPacket() {
        return new PacketWrapper(PacketTypeClasses.Server.SCOREBOARD_DISPLAY_OBJECTIVE)
                .setFieldByIndex(int.class, 0, DISPLAY_SLOT_TO_ID.get(this.displaySlot))
                .setFieldByIndex(String.class, 0, this.objective)
                .getPacket();
    }
}
