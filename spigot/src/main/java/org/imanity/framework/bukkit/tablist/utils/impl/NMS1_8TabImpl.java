package org.imanity.framework.bukkit.tablist.utils.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.tablist.ImanityTabCommons;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.tablist.utils.*;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.version.PlayerVersion;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.ConstructorWrapper;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.FieldWrapper;

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
        Bukkit.getOnlinePlayers().forEach(online -> sendPacket(online, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ( (CraftPlayer) player).getHandle())));
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        GameProfile profile = new GameProfile(UUID.randomUUID(), playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtils.tabEntrys.get(rawSlot - 1) + "");

        if (playerVersion != PlayerVersion.v1_7) {
            profile.getProperties().put("textures", new Property("textures", ImanityTabCommons.defaultTexture.SKIN_VALUE, ImanityTabCommons.defaultTexture.SKIN_SIGNATURE));
        }

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        INFO_ACTION_FIELD.set(packetPlayOutPlayerInfo, PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        INFO_DATA_FIELD.get(packetPlayOutPlayerInfo).add(INFO_DATA_CONSTRUCTOR.newInstance(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, IChatBaseComponent.ChatSerializer.a(fromText(""))));
        sendPacket(player, packetPlayOutPlayerInfo);

        return new TabEntry(string, profile.getId(), "", imanityTablist, ImanityTabCommons.defaultTexture, column, slot, rawSlot, 0);
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
            Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot() - 1));
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot() - 1));
            }
            team.setPrefix(Utility.color(newStrings[0]));
            if (newStrings.length > 1) {
                team.setSuffix(Utility.color(newStrings[1]));
            } else {
                team.setSuffix("");
            }

            team.addEntry(LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
        } else {
            IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(Utility.color(text)));

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
    public void updateFakeSkin(ImanityTablist imanityTablist, TabEntry tabEntry, SkinTexture skinTexture) {
        if (tabEntry.getTexture() == skinTexture){
            return;
        }

        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(imanityTablist.getPlayer());
        if (playerVersion == PlayerVersion.v1_7) {
            return;
        }

        GameProfile gameProfile = this.getGameProfile(playerVersion, tabEntry);

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", new Property("textures", skinTexture.SKIN_VALUE, skinTexture.SKIN_SIGNATURE));

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

        tabEntry.setTexture(skinTexture);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist imanityTablist, String header, String footer) {

    }

    private void sendPacket(Player player, Packet packet) {
        getEntity(player).playerConnection.sendPacket(packet);
    }

    private EntityPlayer getEntity(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private GameProfile getGameProfile(PlayerVersion playerVersion, TabEntry tabEntry) {
        return new GameProfile(tabEntry.getUuid(), playerVersion != PlayerVersion.v1_7  ? "0" + (tabEntry.getRawSlot() > 9 ? tabEntry.getRawSlot() : "0" + tabEntry.getRawSlot()) + "|Tab" : LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
    }

    private static String fromText(String text) {
        return ComponentSerializer.toString(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text)));
    }
}
