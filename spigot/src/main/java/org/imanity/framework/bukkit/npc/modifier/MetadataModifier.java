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

package org.imanity.framework.bukkit.npc.modifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.npc.NPC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MetadataModifier extends NPCModifier {

    private final List<WrappedWatchableObject> metadata = new ArrayList<>();

    public MetadataModifier(@NotNull NPC npc) {
        super(npc);
    }

    public <I, O> MetadataModifier queue(@NotNull EntityMetadata<I, O> metadata, @NotNull I value) {
        return this.queue(metadata.getIndex(), metadata.getMapper().apply(value), metadata.getOutputType());
    }

    public <T> MetadataModifier queue(int index, @NotNull T value, @NotNull Class<T> clazz) {
        return this.queue(index, value, MINECRAFT_VERSION < 9 ? null : WrappedDataWatcher.Registry.get(clazz));
    }

    public <T> MetadataModifier queue(int index, @NotNull T value, @Nullable WrappedDataWatcher.Serializer serializer) {
        this.metadata.add(
                serializer == null ?
                        new WrappedWatchableObject(
                                index,
                                value
                        ) :
                        new WrappedWatchableObject(
                                new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer),
                                value
                        )
        );

        return this;
    }

    @Override
    public void send(@NotNull Player... targetPlayers) {
        PacketContainer packetContainer = super.newContainer(PacketType.Play.Server.ENTITY_METADATA);

        packetContainer.getWatchableCollectionModifier().write(0, this.metadata);

        super.send(targetPlayers);
    }

    public static class EntityMetadata<I, O> {

        public static final EntityMetadata<Boolean, Byte> SNEAKING = new EntityMetadata<>(
                0,
                Byte.class,
                Collections.emptyList(),
                input -> (byte) (input ? 0x02 : 0)
        );

        public static final EntityMetadata<Boolean, Byte> SKIN_LAYERS = new EntityMetadata<>(
                10,
                Byte.class,
                Arrays.asList(9, 9, 10, 14, 14, 15),
                input -> (byte) (input ? 0xff : 0)
        );

        private final int baseIndex;

        private final Class<O> outputType;

        private final Collection<Integer> shiftVersions;

        private final Function<I, O> mapper;

        EntityMetadata(int baseIndex, Class<O> outputType, Collection<Integer> shiftVersions, Function<I, O> mapper) {
            this.baseIndex = baseIndex;
            this.outputType = outputType;
            this.shiftVersions = shiftVersions;
            this.mapper = mapper;
        }

        public int getIndex() {
            return this.baseIndex + Math.toIntExact(this.shiftVersions.stream().filter(minor -> NPCModifier.MINECRAFT_VERSION >= minor).count());
        }

        public Class<O> getOutputType() {
            return outputType;
        }

        public Function<I, O> getMapper() {
            return mapper;
        }

    }

}
