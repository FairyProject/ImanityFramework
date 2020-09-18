package org.imanity.framework.bukkit.packet.wrapper.server;

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

@AutowiredWrappedPacket(value = PacketType.Server.PLAYER_LIST_HEADER_FOOTER, direction = PacketDirection.WRITE)
@Getter
@Setter
public class WrappedPacketOutPlayerListHeaderAndFooter extends WrappedPacket implements SendableWrapper {

    private ChatComponentWrapper header;
    private ChatComponentWrapper footer;

    public WrappedPacketOutPlayerListHeaderAndFooter(Object packet) {
        super(packet);
    }

    public WrappedPacketOutPlayerListHeaderAndFooter(ChatComponentWrapper header, ChatComponentWrapper footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    protected void setup() {
        this.header = readChatComponent(0);
        this.footer = readChatComponent(1);
    }

    @Override
    public Object asNMSPacket() {
        try {

            Object packet = PacketTypeClasses.Server.PLAYER_LIST_HEADER_FOOTER.newInstance();
            PacketWrapper objectWrapper = new PacketWrapper(packet);

            objectWrapper.setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 0, MinecraftReflection.getChatComponentConverter().getGeneric(this.header));
            objectWrapper.setFieldByIndex(MinecraftReflection.getIChatBaseComponentClass(), 1, MinecraftReflection.getChatComponentConverter().getGeneric(this.footer));

            return packet;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
