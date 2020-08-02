package org.imanity.framework.bukkit.util;

import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.util.Arrays;
import java.util.Iterator;

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

}
