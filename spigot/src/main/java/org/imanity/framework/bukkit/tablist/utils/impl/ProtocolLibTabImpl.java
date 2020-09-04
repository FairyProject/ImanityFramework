package org.imanity.framework.bukkit.tablist.utils.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.tablist.ImanityTabCommons;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.tablist.utils.*;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.version.PlayerVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class ProtocolLibTabImpl implements IImanityTabImpl {

    public ProtocolLibTabImpl() {
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist tablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        UUID uuid = UUID.randomUUID();
        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        WrappedGameProfile profile = new WrappedGameProfile(uuid, playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtils.tabEntrys.get(rawSlot - 1) + "");
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerVersion != PlayerVersion.v1_7 ?  "" : profile.getName()));
        if (playerVersion != PlayerVersion.v1_7) {
            playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", ImanityTabCommons.defaultTexture.SKIN_VALUE, ImanityTabCommons.defaultTexture.SKIN_SIGNATURE));
        }
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(player, packet);
        return new TabEntry(string, uuid, "", tablist, ImanityTabCommons.defaultTexture, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist tablist, TabEntry tabEntry, String text) {
        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);
        String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());
        if (playerVersion == PlayerVersion.v1_7) {
            Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot()-1));
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot()-1));
            }
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', newStrings[0]));
            if (newStrings.length > 1) {
                team.setSuffix(ChatColor.translateAlternateColorCodes('&', newStrings[1]));
            }else{
                team.setSuffix("");
            }
        }else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME);
            WrappedGameProfile profile = new WrappedGameProfile(
                    tabEntry.getUuid(),
                    tabEntry.getId()
            );
            PlayerInfoData playerInfoData = new PlayerInfoData(
                    profile,
                    1,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', newStrings.length > 1 ? newStrings[0] + newStrings[1] : newStrings[0]))
            );
            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            sendPacket(player, packet);
        }
        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist tablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) {
            return;
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_LATENCY);

        WrappedGameProfile profile = new WrappedGameProfile(
                tabEntry.getUuid(),
                tabEntry.getId()
        );

        PlayerInfoData playerInfoData = new PlayerInfoData(
                profile,
                latency,
                EnumWrappers.NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', tabEntry.getText()))
        );

        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(tablist.getPlayer(), packet);
        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist zigguratTablist, TabEntry tabEntry, SkinTexture skinTexture) {
        if (tabEntry.getTexture() == skinTexture) {
            return;
        }

        final Player player = zigguratTablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        WrappedGameProfile profile = new WrappedGameProfile(tabEntry.getUuid(), playerVersion != PlayerVersion.v1_7  ? tabEntry.getId() : LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerVersion != PlayerVersion.v1_7 ?  "" : profile.getName()));

        if (playerVersion != PlayerVersion.v1_7) {
            playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", skinTexture.SKIN_VALUE, skinTexture.SKIN_SIGNATURE));
        }

        PacketContainer remove = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        remove.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        remove.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));


        PacketContainer add = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        add.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        add.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        sendPacket(player, remove);
        sendPacket(player, add);

        tabEntry.setTexture(skinTexture);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist tablist, String header, String footer) {
        PacketContainer headerAndFooter = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        if (playerVersion != PlayerVersion.v1_7) {

            headerAndFooter.getChatComponents().write(0, WrappedChatComponent.fromText(header));
            headerAndFooter.getChatComponents().write(1, WrappedChatComponent.fromText(footer));

            sendPacket(player, headerAndFooter);
        }
    }

    private static void sendPacket(Player player, PacketContainer packetContainer){
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
