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
import org.imanity.framework.bukkit.tablist.utils.player.PlayerUtil;
import org.imanity.framework.bukkit.tablist.utils.version.PlayerVersion;

import java.util.UUID;

public class NMS1_8TabImpl implements IImanityTabImpl {

    @Override
    public void removeSelf(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> sendPacket(online, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ( (CraftPlayer) player).getHandle())));
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = PlayerUtil.getPlayerVersion(player);

        GameProfile profile = new GameProfile(UUID.randomUUID(), playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtils.tabEntrys.get(rawSlot - 1) + "");

        if (playerVersion != PlayerVersion.v1_7) {
            profile.getProperties().put("textures", new Property("textures", ImanityTabCommons.defaultTexture.SKIN_VALUE, ImanityTabCommons.defaultTexture.SKIN_SIGNATURE));
        }

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.setA(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        packetPlayOutPlayerInfo.getB().add(new PacketPlayOutPlayerInfo.PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, IChatBaseComponent.ChatSerializer.a(fromText(""))));
        sendPacket(player, packetPlayOutPlayerInfo);

        return new TabEntry(string, profile, "", imanityTablist, ImanityTabCommons.defaultTexture, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist imanityTablist, TabEntry tabEntry, String text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = PlayerUtil.getPlayerVersion(player);

        String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());

        if (playerVersion == PlayerVersion.v1_7) {
            Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot() - 1));
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot() - 1));
            }
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', newStrings[0]));
            if (newStrings.length > 1) {
                team.setSuffix(ChatColor.translateAlternateColorCodes('&', newStrings[1]));
            } else {
                team.setSuffix("");
            }
        } else {
            IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(Utility.color(text)));

            PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
            packetPlayOutPlayerInfo.setA(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
            packetPlayOutPlayerInfo.getB().add(new PacketPlayOutPlayerInfo.PlayerInfoData(tabEntry.getGameProfile(), tabEntry.getLatency(), WorldSettings.EnumGamemode.SURVIVAL, listName));
            sendPacket(player, packetPlayOutPlayerInfo);
        }

        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist imanityTablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) return;

        IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(tabEntry.getText()));

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.setA(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY);
        packetPlayOutPlayerInfo.getB().add(new PacketPlayOutPlayerInfo.PlayerInfoData(tabEntry.getGameProfile(), latency, WorldSettings.EnumGamemode.SURVIVAL, listName));
        sendPacket(imanityTablist.getPlayer(), packetPlayOutPlayerInfo);

        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist imanityTablist, TabEntry tabEntry, SkinTexture skinTexture) {
        if (tabEntry.getTexture() == skinTexture){
            return;
        }

        GameProfile gameProfile = tabEntry.getGameProfile();

        gameProfile.getProperties().clear();
        gameProfile.getProperties().put("textures", new Property("textures", skinTexture.SKIN_VALUE, skinTexture.SKIN_SIGNATURE));

        IChatBaseComponent listName = ChatComponentText.ChatSerializer.a(fromText(tabEntry.getText()));

        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(tabEntry.getGameProfile(), tabEntry.getLatency(), WorldSettings.EnumGamemode.SURVIVAL, listName);

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.setA(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        packetPlayOutPlayerInfo.getB().add(playerInfoData);
        sendPacket(imanityTablist.getPlayer(), packetPlayOutPlayerInfo);

        packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        packetPlayOutPlayerInfo.setA(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        packetPlayOutPlayerInfo.getB().add(playerInfoData);
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

    private static String fromText(String text) {
        return ComponentSerializer.toString(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text)));
    }
}
