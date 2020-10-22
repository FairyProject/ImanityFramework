/*
 * MIT License
 *
 * Copyright (c) 2020 - 2020 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
