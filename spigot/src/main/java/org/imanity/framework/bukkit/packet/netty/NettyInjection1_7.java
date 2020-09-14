package org.imanity.framework.bukkit.packet.netty;

import net.minecraft.util.io.netty.channel.*;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.packet.PacketService;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.plugin.service.Autowired;

public class NettyInjection1_7 implements INettyInjection {

    @Autowired
    private PacketService packetService;

    @Override
    public void inject(Player player) {
        ChannelDuplexHandler duplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Object packet = packetService.read(player, msg);
                if (packet == null) {
                    return;
                }
                super.channelRead(ctx, packet);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                Object packet = packetService.write(player, msg);
                if (packet == null) {
                    return;
                }
                super.write(ctx, packet, promise);
            }
        };
        Channel channel = MinecraftReflection.getChannel(player);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addBefore("packet_handler", PacketService.CHANNEL_HANDLER, duplexHandler);
    }

    @Override
    public void eject(Player player) {
        Channel channel = MinecraftReflection.getChannel(player);
        channel.pipeline().remove(PacketService.CHANNEL_HANDLER);
    }
}
