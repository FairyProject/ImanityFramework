package org.imanity.framework.bukkit.packet.wrapper.server;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.imanity.framework.bukkit.packet.PacketDirection;
import org.imanity.framework.bukkit.packet.type.PacketType;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.SendableWrapper;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.annotation.AutowiredWrappedPacket;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.wrapper.ChatComponentWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.PacketWrapper;

@AutowiredWrappedPacket(value = PacketType.Server.TITLE, direction = PacketDirection.WRITE)
@Getter
@Setter
@Builder
public class WrappedPacketOutTitle extends WrappedPacket implements SendableWrapper {

    public static final int DEFAULT_FADE_IN = 20;
    public static final int DEFAULT_STAY = 200;
    public static final int DEFAULT_FADE_OUT = 20;

    private Action action;
    private ChatComponentWrapper message;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public WrappedPacketOutTitle(Object packet) {
        super(packet);
    }

    @Override
    protected void setup() {

        this.action = MinecraftReflection.getTitleActionConverter().getSpecific(readObject(0, MinecraftReflection.getEnumTitleActionClass()));
        this.message = readChatComponent(0);

        this.fadeIn = readInt(0);
        this.stay = readInt(1);
        this.fadeOut = readInt(2);

    }

    @Override
    public Object asNMSPacket() {
        return new PacketWrapper(PacketTypeClasses.Server.TITLE)
                .setFieldByIndex(MinecraftReflection.getEnumTitleActionClass(), 0, MinecraftReflection.getTitleActionConverter().getGeneric(this.action))
                .setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 0, this.message.getHandle())
                .setFieldByIndex(int.class, 0, this.fadeIn)
                .setFieldByIndex(int.class, 1, this.stay)
                .setFieldByIndex(int.class, 2, this.fadeOut)
                .getPacket();
    }

    public static enum Action {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET;
    }

}
