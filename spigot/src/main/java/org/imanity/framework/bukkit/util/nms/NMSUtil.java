package org.imanity.framework.bukkit.util.nms;

import net.minecraft.server.v1_8_R3.*;

import java.lang.reflect.Field;
import java.util.Map;

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

    private static Object getPrivateField(final String fieldName, final Class<EntityTypes> oclass)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
    {
        final Field field = oclass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    public static void registerCustomEntity(final Class<? extends Entity> entityClass, final int entityId, String name)
    {
        try
        {
            ((Map) getPrivateField("c", EntityTypes.class)).put(name, entityClass);
        } catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        try
        {
            ((Map) getPrivateField("d", EntityTypes.class)).put(entityClass, name);
        } catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        try
        {
            ((Map) getPrivateField("f", EntityTypes.class)).put(entityClass, entityId);
        } catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        try
        {
            ((Map) getPrivateField("g", EntityTypes.class)).put(name, entityId);
        } catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
    }

}
