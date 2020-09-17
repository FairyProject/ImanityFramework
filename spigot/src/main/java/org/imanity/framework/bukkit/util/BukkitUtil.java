package org.imanity.framework.bukkit.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.imanity.framework.bukkit.reflection.resolver.MethodResolver;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class BukkitUtil {

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

    public static List<Player> getPlayersFromUuids(Collection<UUID> uuids) {
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

    public static void sendMessages(Player player, Iterable<String> iterable) {
        for (String message : iterable) {
            player.sendMessage(message);
        }
    }

    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }

    public static File getPluginJar(JavaPlugin plugin) {

        MethodResolver resolver = new MethodResolver(JavaPlugin.class);
        return (File) resolver.resolveWrapper("getFile").invoke(plugin);

    }

    public static void delayedUpdateInventory(Player player) {
        TaskUtil.runScheduled(() -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        }, 1L);
    }

    public static boolean isPlayerEvent(Class<?> event) {

        if (PlayerEvent.class.isAssignableFrom(event)) {
            return true;
        }

        MethodResolver resolver = new MethodResolver(event);
        return resolver.resolveWrapper("getPlayer").exists();

    }

    public static org.bukkit.block.Block getBlockLookingAt(final Player player, final int distance) {
        final Location location = player.getEyeLocation();
        final BlockIterator blocksToAdd = new BlockIterator(location, 0.0D, distance);
        org.bukkit.block.Block block = null;
        while (blocksToAdd.hasNext()) {
            block = blocksToAdd.next();
        }
        return block;
    }

}
