package org.imanity.framework.bukkit.tablist.util.impl.v1_8;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.Skin;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.tablist.util.*;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.reflection.wrapper.PacketWrapper;
import org.imanity.framework.bukkit.reflection.version.PlayerVersion;
import org.imanity.framework.bukkit.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.reflection.wrapper.ConstructorWrapper;
import org.imanity.framework.bukkit.reflection.wrapper.FieldWrapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NMS1_8TabImpl implements IImanityTabImpl {

    private final FieldWrapper<PacketPlayOutPlayerInfo.EnumPlayerInfoAction> INFO_ACTION_FIELD;
    private final FieldWrapper<List> INFO_DATA_FIELD;

    private final Class<?> INFO_DATA_TYPE;
    private final ConstructorWrapper INFO_DATA_CONSTRUCTOR;

    {
        NMSClassResolver classResolver = new NMSClassResolver();
        FieldResolver resolver = new FieldResolver(PacketPlayOutPlayerInfo.class);
        try {
            INFO_DATA_TYPE = classResolver.resolve("PacketPlayOutPlayerInfo$PlayerInfoData");
            INFO_DATA_CONSTRUCTOR = new ConstructorWrapper(INFO_DATA_TYPE.getConstructor(GameProfile.class, int.class, WorldSettings.EnumGamemode.class, IChatBaseComponent.class));

            INFO_ACTION_FIELD = resolver.resolveByFirstTypeWrapper(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.class);
            INFO_DATA_FIELD = resolver.resolveByFirstTypeWrapper(List.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSelf(Player player) {
        Imanity.getPlayers()
                .stream()
                .filter(online -> MinecraftReflection.getProtocol(online) == PlayerVersion.v1_7)
                .forEach(online -> sendPacket(online, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ( (CraftPlayer) player).getHandle())));
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        GameProfile profile = new GameProfile(UUID.randomUUID(), playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtil.entry(rawSlot - 1) + "");

        if (playerVersion != PlayerVersion.v1_7) {
            profile.getProperties().put("textures", new Property("textures", Skin.GRAY.skinValue, Skin.GRAY.skinSignature));
        }

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(INFO_DATA_CONSTRUCTOR.newInstance(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, IChatBaseComponent.ChatSerializer.a(fromText(""))));
        sendPacket(player, packetPlayOutPlayerInfo);

        return new TabEntry(string, profile.getId(), "", imanityTablist, Skin.GRAY, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist imanityTablist, TabEntry tabEntry, String text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());

        if (playerVersion == PlayerVersion.v1_7) {

            Imanity.IMPLEMENTATION.sendTeam(
                    player,
                    LegacyClientUtil.name(tabEntry.getRawSlot() - 1),
                    BukkitUtil.color(newStrings[0]),
                    newStrings.length > 1 ? BukkitUtil.color(newStrings[1]) : "",
                    Collections.singleton(LegacyClientUtil.entry(tabEntry.getRawSlot() - 1)),
                    2
            );

        } else {
            IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(BukkitUtil.color(text)));

            GameProfile profile = this.getGameProfile(playerVersion, tabEntry);

            PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
            INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
            INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(INFO_DATA_CONSTRUCTOR.newInstance(profile, tabEntry.getLatency(), WorldSettings.EnumGamemode.SURVIVAL, listName));
            sendPacket(player, packetPlayOutPlayerInfo);
        }

        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist imanityTablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) return;

        IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(tabEntry.getText()));

        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(imanityTablist.getPlayer());
        GameProfile profile = this.getGameProfile(playerVersion, tabEntry);

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY);
        INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(INFO_DATA_CONSTRUCTOR.newInstance(profile, latency, WorldSettings.EnumGamemode.SURVIVAL, listName));
        sendPacket(imanityTablist.getPlayer(), packetPlayOutPlayerInfo);

        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist imanityTablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture().equals(skin)){
            return;
        }

        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(imanityTablist.getPlayer());
        if (playerVersion == PlayerVersion.v1_7) {
            return;
        }

        GameProfile gameProfile = this.getGameProfile(playerVersion, tabEntry);

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", new Property("textures", skin.skinValue, skin.skinSignature));

        IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(tabEntry.getText()));

        Object playerInfoData = INFO_DATA_CONSTRUCTOR.newInstance(gameProfile, tabEntry.getLatency(), WorldSettings.EnumGamemode.SURVIVAL, listName);

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(playerInfoData);
        sendPacket(imanityTablist.getPlayer(), packetPlayOutPlayerInfo);

        packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(playerInfoData);
        sendPacket(imanityTablist.getPlayer(), packetPlayOutPlayerInfo);

        tabEntry.setTexture(skin);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist imanityTablist, String header, String footer) {

        Player player = imanityTablist.getPlayer();
        if (MinecraftReflection.getProtocol(player) == PlayerVersion.v1_7) {
            return;
        }

        PacketWrapper packet = new PacketWrapper(new PacketPlayOutPlayerListHeaderFooter());

        final IChatBaseComponent tabHeader = IChatBaseComponent.ChatSerializer.a("{text: '" + header + "'}");
        final IChatBaseComponent tabFooter = IChatBaseComponent.ChatSerializer.a("{text: '" + footer + "'}");

        packet.setPacketValue("a", tabHeader);
        packet.setPacketValue("b", tabFooter);

        MinecraftReflection.sendPacket(player, packet);

    }

    private void sendPacket(Player player, Packet packet) {
        getEntity(player).playerConnection.sendPacket(packet);
    }

    private EntityPlayer getEntity(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private GameProfile getGameProfile(PlayerVersion playerVersion, TabEntry tabEntry) {
        return new GameProfile(tabEntry.getUuid(), playerVersion != PlayerVersion.v1_7  ? tabEntry.getId() : LegacyClientUtil.entry(tabEntry.getRawSlot() - 1) + "");
    }

    private static String fromText(String text) {
        return ComponentSerializer.toString(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text)));
    }
}
