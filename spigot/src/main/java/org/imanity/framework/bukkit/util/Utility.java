package org.imanity.framework.bukkit.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import spg.lgdev.knockback.impl.RegularKnockback;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Utility {

    private static final int INDEX_NOT_FOUND = -1;

    public static <T> String joinToString(final T[] array) {
        return array == null ? "null" : joinToString(Arrays.asList(array));
    }

    public static <T> String joinToString(final T[] array, final String delimiter) {
        return array == null ? "null" : joinToString(Arrays.asList(array), delimiter);
    }

    public static <T> String joinToString(final Iterable<T> array) {
        return array == null ? "null" : joinToString(array, ", ");
    }

    public static <T> String joinToString(final Iterable<T> array, final String delimiter) {
        return join(array, delimiter, object -> object == null ? "" : object.toString());
    }

    public static <T> String join(final Iterable<T> array, final String delimiter, final Stringer<T> stringer) {
        final Iterator<T> it = array.iterator();
        String message = "";

        while (it.hasNext()) {
            final T next = it.next();

            if (next != null)
                message += stringer.toString(next) + (it.hasNext() ? delimiter : "");
        }

        return message;
    }

    public interface Stringer<T> {

        /**
         * Convert the given object into a string
         *
         * @param object
         * @return
         */
        String toString(T object);
    }

    public static Iterable<String> toStringList(String string, final String delimiter) {
        return Arrays.asList(string.split(delimiter));
    }

    public static void error(Throwable ex, String message) {
        throw new RuntimeException(message, ex);
    }

    public static String color(String string) {
        return string == null || string.isEmpty() ? "" :
                ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void setBlockInNativeDataPalette(World world, int x, int y, int z, int blockId, byte data) {
        net.minecraft.server.v1_8_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.server.v1_8_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));

        ChunkSection cs = nmsChunk.getSections()[y >> 4];

        cs.setType(x & 15, y & 15, z & 15, ibd);
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String replace(final String text, final String searchString, final String replacement) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null)
            return text;
        final String searchText = text;
        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND)
            return text;
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= 16;
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            end = searchText.indexOf(searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    public static String replace(final String text, final String searchString, final Object replacement) {
        return replace(text, searchString, replacement.toString());
    }

    public static String replace(String text, final RV... replaceValues) {
        for (final RV replaceValue : replaceValues) {
            text = ChatColor.translateAlternateColorCodes('&', replace(text, replaceValue.getTarget(), replaceValue.getReplacement()));
        }
        return text;
    }

    public static List<Player> getPlayersFromUuids(List<UUID> uuids) {
        return uuids
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    public static Player getDamager(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = damageByEntityEvent.getDamager();

            if (damager instanceof Player) {
                return (Player) damageByEntityEvent.getDamager();
            }

            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player) {
                    return (Player) shooter;
                }
            }
        }
        return null;
    }

    public static String getProgressBar(int current, int max, int totalBars, String symbol, String completedColor, String notCompletedColor){

        float percent = (float) current / max;

        int progressBars = (int) ((int) totalBars * percent);

        int leftOver = (totalBars - progressBars);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.translateAlternateColorCodes('&', completedColor));
        for (int i = 0; i < progressBars; i++) {
            sb.append(symbol);
        }
        sb.append(ChatColor.translateAlternateColorCodes('&', notCompletedColor));
        for (int i = 0; i < leftOver; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static void clear(final Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0.0f);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(type -> player.removePotionEffect(type));
    }

    public static void showDyingNPC(Player player) {
        Location loc = player.getLocation();
        final List<Player> players = new ArrayList<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other != player && other.getWorld() == player.getWorld() && other.getLocation().distanceSquared(loc) < 32 * 32) {
                players.add(other);
            }
        }
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        int i = net.minecraft.server.v1_8_R3.Entity.getEntityCount() + 1;
        net.minecraft.server.v1_8_R3.Entity.setEntityCount(i);
        packet.setA(i);
        PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus();
        statusPacket.setA(i);
        statusPacket.setB((byte) 3);
        PacketPlayOutEntityDestroy destoryPacket = new PacketPlayOutEntityDestroy(i);

        for (Player other : players) {
            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.add(i);
            ((CraftPlayer) other).getHandle().playerConnection.sendPacket(packet);
            ((CraftPlayer) other).getHandle().playerConnection.sendPacket(statusPacket);
        }

        TaskUtil.runScheduled(() -> players.forEach(other -> {
            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.remove(i);
            ((CraftPlayer) other).getHandle().playerConnection.sendPacket(destoryPacket);
        }), 20L);
    }

}
