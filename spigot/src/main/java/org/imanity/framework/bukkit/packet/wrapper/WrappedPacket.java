/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
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

package org.imanity.framework.bukkit.packet.wrapper;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.OBCClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.MethodWrapper;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.PacketWrapper;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Getter
public class WrappedPacket implements WrapperPacketReader {

    private static final Class<?> NMS_ITEM_STACK;
    private static final MethodWrapper<ItemStack> ITEM_COPY_OF_METHOD;

    public static final NMSClassResolver NMS_CLASS_RESOLVER = new NMSClassResolver();
    public static final OBCClassResolver CRAFT_CLASS_RESOLVER = new OBCClassResolver();

    static {
        try {
            NMS_ITEM_STACK = NMS_CLASS_RESOLVER.resolve("ItemStack");

            Class<?> type = CRAFT_CLASS_RESOLVER.resolve("CraftItemStack");
            ITEM_COPY_OF_METHOD = new MethodWrapper<>(type.getDeclaredMethod("asBukkitCopy"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected final List<Field> fields = new ArrayList<>();
    protected final Player player;

    protected PacketWrapper packet;
    private Class<?> packetClass;

    public WrappedPacket() {
        this(null);
    }

    public WrappedPacket(final Object packet) {
        this(null, packet);
    }

    public WrappedPacket(final Player player, final Object packet) {
        if(packet == null) {
            this.player = null;
            return;
        }
        this.packetClass = packet.getClass();

        if (packet.getClass().getSuperclass().equals(PacketTypeClasses.Client.FLYING)) {
            packetClass = PacketTypeClasses.Client.FLYING;
        } else if (packet.getClass().getSuperclass().equals(PacketTypeClasses.Server.ENTITY)) {
            packetClass = PacketTypeClasses.Server.ENTITY;
        }

        for (Field f : packetClass.getDeclaredFields()) {
            f.setAccessible(true);
            fields.add(f);
        }
        this.player = player;
        this.packet = new PacketWrapper(packet);
        setup();
    }

    protected void setup() {

    }

    public <T extends WrappedPacket> T wrap(Class<T> type) {
        try {
            return type.cast(this);
        } catch (ClassCastException ex) {
            throw new IllegalStateException("Couldn't convert current wrapper " + this.getClass().getSimpleName() + " to " + type.getSimpleName());
        }
    }

    public World getWorld() {
        return this.player != null ? this.player.getWorld() : null;
    }

    @Override
    public boolean readBoolean(int index) {
        return (boolean) readObject(index, boolean.class);
    }

    @Override
    public byte readByte(int index) {
        return (byte) readObject(index, byte.class);
    }

    @Override
    public short readShort(int index) {
        return (short) readObject(index, short.class);
    }

    @Override
    public int readInt(int index) {
        return (int) readObject(index, int.class);
    }

    @Override
    public long readLong(int index) {
        return (long) readObject(index, long.class);
    }

    @Override
    public float readFloat(int index) {
        return (float) readObject(index, float.class);
    }

    @Override
    public double readDouble(int index) {
        return (double) readObject(index, double.class);
    }

    public ItemStack readItemStack(int index) {
        return ITEM_COPY_OF_METHOD.invoke(null, readObject(index, NMS_ITEM_STACK));
    }

    @Override
    public Object readObject(int index, Class<?> type) {
        return this.packet.setPacketValueByType(type, index);
    }

    @Override
    public Object readAnyObject(int index) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        int currentIndex = 0;
        for (Field f : fields) {
            if (index == currentIndex++) {
                try {
                    return lookup.unreflectGetter(f).invoke(packet.getPacket());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String readString(int index) {
        return readObject(index, String.class).toString();
    }
}
