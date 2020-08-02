package org.imanity.framework.bukkit.util.nms;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Vector3f;

public class NMSUtil {

    public static void setDataWatcher(DataWatcher dataWatcher, int index, Object value) {
        int type = getValueType(value);

        dataWatcher.dataValues.put(index, new DataWatcher.WatchableObject(type, index, value));
    }

    public static int getValueType(Object value) {
        int type = 0;
        if (value instanceof Number) {
            if (value instanceof Byte) {
                type = 0;
            } else if (value instanceof Short) {
                type = 1;
            } else if (value instanceof Integer) {
                type = 2;
            } else if (value instanceof Float) {
                type = 3;
            }
        } else if (value instanceof String) {
            type = 4;
        } else if (value != null && value.getClass().equals(ItemStack.class)) {
            type = 5;
        } else if (value != null && value.getClass().equals(BlockPosition.class)) {
            type = 6;
        } else if (value != null && value.getClass().equals(Vector3f.class)) {
            type = 7;
        }

        return type;
    }

}
