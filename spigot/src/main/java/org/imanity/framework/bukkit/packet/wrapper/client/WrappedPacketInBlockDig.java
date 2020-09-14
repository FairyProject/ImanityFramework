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

package org.imanity.framework.bukkit.packet.wrapper.client;

import org.imanity.framework.bukkit.packet.type.PacketTypeClasses;
import org.imanity.framework.bukkit.packet.wrapper.WrappedPacket;
import org.imanity.framework.bukkit.packet.wrapper.other.EnumDirection;
import org.imanity.framework.bukkit.packet.wrapper.other.Vector3D;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.MinecraftVersion;
import org.imanity.framework.bukkit.util.reflection.Reflection;

public final class WrappedPacketInBlockDig extends WrappedPacket {
    private static Class<?> blockDigClass, blockPositionClass, enumDirectionClass, digTypeClass;
    private static boolean isVersionLowerThan_v_1_8;
    private BlockPosition blockPosition;
    private EnumDirection direction;
    private PlayerDigType digType;
    public WrappedPacketInBlockDig(Object packet) {
        super(packet);
    }

    public static void load() {
        blockDigClass = PacketTypeClasses.Client.BLOCK_DIG;
        try {
            if (MinecraftVersion.newerThan(MinecraftVersion.V.v1_7)) {
                blockPositionClass = PACKET_FIELD_CLASS_RESOLVER.resolve("BlockPosition");
                enumDirectionClass = PACKET_FIELD_CLASS_RESOLVER.resolve("EnumDirection");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        isVersionLowerThan_v_1_8 = MinecraftVersion.olderThan(MinecraftVersion.V.v1_8);

        if (!MinecraftVersion.equals(MinecraftVersion.V.v1_7)) {
            try {
                digTypeClass = PACKET_FIELD_CLASS_RESOLVER.resolve("EnumPlayerDigType");
            } catch (ClassNotFoundException e) {
                //It is probably a subclass
                digTypeClass = PACKET_FIELD_CLASS_RESOLVER.resolveSilent(blockDigClass.getSimpleName() + "$EnumPlayerDigType");
            }
        }
    }

    @Override
    protected void setup() {
        EnumDirection enumDirection = null;
        PlayerDigType enumDigType = null;
        int x = 0, y = 0, z = 0;
        //1.7.10
        try {
            if (isVersionLowerThan_v_1_8) {
                enumDigType = PlayerDigType.values()[(Reflection.getField(blockDigClass, int.class, 4).getInt(packet))];
                x = readInt(0);
                y = readInt(1);
                z = readInt(2);
                enumDirection = null;
            } else {
                //1.8+
                final Object blockPosObj = readObject(0, blockPositionClass);
                final Object enumDirectionObj = readObject(0, enumDirectionClass);
                final Object digTypeObj = readObject(0, digTypeClass);

                Class<?> blockPosSuper = blockPositionClass;
                x = Reflection.getField(blockPosSuper, int.class, 0).getInt(blockPosObj);
                y = Reflection.getField(blockPosSuper, int.class, 1).getInt(blockPosObj);
                z = Reflection.getField(blockPosSuper, int.class, 2).getInt(blockPosObj);

                //.toString() won't work so we must do this
                enumDirection = EnumDirection.valueOf(((Enum)enumDirectionObj).name());
                enumDigType = PlayerDigType.valueOf(((Enum)digTypeObj).name());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.blockPosition = new BlockPosition(x, y, z);
        if (enumDirection == null) {
            this.direction = EnumDirection.NULL;
        } else {
            this.direction = enumDirection;
        }
        this.digType = enumDigType;
    }

    /**
     * Get X position of the block
     * @return Block Position X
     */
    public int getBlockPositionX() {
        return blockPosition.x;
    }

    /**
     * Get Y position of the block
     * @return Block Position Y
     */
    public int getBlockPositionY() {
        return blockPosition.y;
    }

    /**
     * Get Z position of the block
     * @return Block Position Z
     */
    public int getBlockPositionZ() {
        return blockPosition.z;
    }

    /**
     * Use {@link #getBlockPositionX()}, {@link #getBlockPositionY()}, {@link #getBlockPositionZ()}
     * @return Block Position
     */
    @Deprecated
    public Vector3i getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get the direction
     * Is Direction.NULL on 1.7.10 FOR NOW
     * @return Direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the PlayerDigType enum sent in this packet.
     * @return Dig Type
     */
    public PlayerDigType getDigType() {
        return digType;
    }

    public enum PlayerDigType {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_HELD_ITEMS,
        SWAP_ITEM_WITH_OFFHAND,
        WRONG_PACKET
    }


}
