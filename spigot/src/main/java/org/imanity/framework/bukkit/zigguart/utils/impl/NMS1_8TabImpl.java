package org.imanity.framework.bukkit.zigguart.utils.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.imanity.framework.bukkit.util.Utility;
import org.imanity.framework.bukkit.zigguart.ImanityTabCommons;
import org.imanity.framework.bukkit.zigguart.ImanityTablist;
import org.imanity.framework.bukkit.zigguart.utils.*;
import org.imanity.framework.bukkit.zigguart.utils.player.PlayerUtil;
import org.imanity.framework.bukkit.zigguart.utils.version.PlayerVersion;

import java.util.Map;
import java.util.UUID;

public class NMS1_8TabImpl implements IImanityTabImpl {

    private static final MinecraftServer server = MinecraftServer.getServer();
    private static final WorldServer world = server.getWorldServer(0);
    private static final PlayerInteractManager manager = new PlayerInteractManager(world);

    @Override
    public void removeSelf(Player player) {
        Bukkit.getOnlinePlayers().forEach(online -> sendPacket(online, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ( (CraftPlayer) player).getHandle())));
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist imanityTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final OfflinePlayer offlinePlayer = new OfflinePlayer() {
            private final UUID uuid = UUID.randomUUID();

            @Override
            public boolean isOnline() {
                return true;
            }

            @Override
            public String getName() {
                return string;
            }

            @Override
            public UUID getUniqueId() {
                return uuid;
            }

            @Override
            public boolean isBanned() {
                return false;
            }

            @Override
            public void setBanned(boolean b) {

            }

            @Override
            public boolean isWhitelisted() {
                return false;
            }

            @Override
            public void setWhitelisted(boolean b) {

            }

            @Override
            public Player getPlayer() {
                return null;
            }

            @Override
            public long getFirstPlayed() {
                return 0;
            }

            @Override
            public long getLastPlayed() {
                return 0;
            }

            @Override
            public boolean hasPlayedBefore() {
                return false;
            }

            @Override
            public Location getBedSpawnLocation() {
                return null;
            }

            @Override
            public Map<String, Object> serialize() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        };
        final Player player = imanityTablist.getPlayer();
        final PlayerVersion playerVersion = PlayerUtil.getPlayerVersion(player);

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtils.tabEntrys.get(rawSlot - 1) + "");

        EntityPlayer entityPlayer = new EntityPlayer(server, world, profile, manager);

        if (playerVersion != PlayerVersion.v1_7) {
            profile.getProperties().put("textures", new Property("textures", ImanityTabCommons.defaultTexture.SKIN_VALUE, ImanityTabCommons.defaultTexture.SKIN_SIGNATURE));
        }

        entityPlayer.listName = ChatComponentText.ChatSerializer.a(fromText(""));
        entityPlayer.ping = 1;

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        sendPacket(player, packetPlayOutPlayerInfo);

        return new TabEntry(string, offlinePlayer, "", imanityTablist, ImanityTabCommons.defaultTexture, column, slot, rawSlot, 0);
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
            GameProfile profile = new GameProfile(
                    tabEntry.getOfflinePlayer().getUniqueId(),
                    tabEntry.getId()
            );

            EntityPlayer entity = new EntityPlayer(server, world, profile, manager);

            entity.listName = ChatComponentText.ChatSerializer.a(fromText(Utility.color(newStrings.length > 1 ? newStrings[0] + newStrings[1] : newStrings[0])));

            PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, entity);

            sendPacket(player, packetPlayOutPlayerInfo);
        }

        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist imanityTablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) return;

        GameProfile profile = new GameProfile(
                tabEntry.getOfflinePlayer().getUniqueId(),
                LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + ""
        );

        EntityPlayer entity = new EntityPlayer(server, world, profile, manager);
        entity.ping = latency;

        sendPacket(imanityTablist.getPlayer(), new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, entity));

        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist imanityTablist, TabEntry tabEntry, SkinTexture skinTexture) {
        if (tabEntry.getTexture() == skinTexture){
            return;
        }
        GameProfile profile = new GameProfile(tabEntry.getOfflinePlayer().getUniqueId(), LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
        EntityPlayer entity = new EntityPlayer(server, world, profile, manager);
        profile.getProperties().put("textures", new Property("textures", skinTexture.SKIN_VALUE, skinTexture.SKIN_SIGNATURE));

        Packet removePlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity);
        sendPacket(imanityTablist.getPlayer(), removePlayer);

        Packet addPlayer = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);
        sendPacket(imanityTablist.getPlayer(), addPlayer);

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
