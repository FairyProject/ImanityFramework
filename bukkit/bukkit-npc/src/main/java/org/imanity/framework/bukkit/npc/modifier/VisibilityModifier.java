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
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.imanity.framework.bukkit.npc.NPC;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class VisibilityModifier extends NPCModifier {

    public VisibilityModifier(@NotNull NPC npc) {
        super(npc);
    }

    public VisibilityModifier queuePlayerListChange(EnumWrappers.PlayerInfoAction action) {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.PLAYER_INFO, false);

        packetContainer.getPlayerInfoAction().write(0, action);

        PlayerInfoData playerInfoData = new PlayerInfoData(
                super.npc.getGameProfile(),
                20,
                EnumWrappers.NativeGameMode.NOT_SET,
                WrappedChatComponent.fromText("")
        );
        packetContainer.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        return this;
    }

    public VisibilityModifier queueSpawn() {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

        packetContainer.getUUIDs().write(0, super.npc.getGameProfile().getUUID());

        double x = super.npc.getLocation().getX();
        double y = super.npc.getLocation().getY();
        double z = super.npc.getLocation().getZ();

        if (MINECRAFT_VERSION < 9) {
            packetContainer.getIntegers()
                    .write(1, (int) Math.floor(x * 32.0D))
                    .write(2, (int) Math.floor(y * 32.0D))
                    .write(3, (int) Math.floor(z * 32.0D));
        } else {
            packetContainer.getDoubles()
                    .write(0, x)
                    .write(1, y)
                    .write(2, z);
        }

        packetContainer.getBytes()
                .write(0, (byte) (super.npc.getLocation().getYaw() * 256F / 360F))
                .write(1, (byte) (super.npc.getLocation().getPitch() * 256F / 360F));

        if (MINECRAFT_VERSION < 15) {
            packetContainer.getDataWatcherModifier().write(0, new WrappedDataWatcher());
        }

        return this;
    }

    public VisibilityModifier queueDestroy() {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.ENTITY_DESTROY, false);
        packetContainer.getIntegerArrays().write(0, new int[]{super.npc.getEntityId()});
        return this;
    }

}
