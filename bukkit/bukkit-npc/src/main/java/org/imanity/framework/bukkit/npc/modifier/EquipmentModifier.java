/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.imanity.framework.bukkit.npc.modifier;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.npc.NPC;
import org.jetbrains.annotations.NotNull;

public class EquipmentModifier extends NPCModifier {

    public EquipmentModifier(@NotNull NPC npc) {
        super(npc);
    }

    public EquipmentModifier queue(@NotNull EnumWrappers.ItemSlot itemSlot, @NotNull ItemStack equipment) {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packetContainer.getItemSlots().write(MINECRAFT_VERSION < 9 ? 1 : 0, itemSlot);
        packetContainer.getItemModifier().write(0, equipment);

        return this;
    }

    public EquipmentModifier queue(int itemSlot, @NotNull ItemStack equipment) {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packetContainer.getIntegers().write(MINECRAFT_VERSION < 9 ? 1 : 0, itemSlot);
        packetContainer.getItemModifier().write(0, equipment);

        return this;
    }

}
