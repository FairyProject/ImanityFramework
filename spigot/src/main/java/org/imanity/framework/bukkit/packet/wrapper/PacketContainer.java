package org.imanity.framework.bukkit.packet.wrapper;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class PacketContainer {

    private final Object mainPacket;

    private final List<Object> extraPackets;

    private PacketContainer(Object mainPacket) {
        this(mainPacket, 0);
    }

    private PacketContainer(Object mainPacket, int expectedExtraPackets) {
        this.mainPacket = mainPacket;
        this.extraPackets = new ArrayList<>(expectedExtraPackets);
    }

    public PacketContainer addExtraPacket(Object packet) {
        this.extraPackets.add(packet);
        return this;
    }

    public PacketContainer addAll(Collection<Object> packets) {
        this.extraPackets.addAll(packets);
        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PacketContainer empty() {
        return new PacketContainer(null);
    }

    public static PacketContainer of(Object mainPacket) {
        return new Builder()
                .mainPacket(mainPacket)
                .build();
    }

    public static class Builder {

        private Object mainPacket;
        private List<Object> extraPackets;

        public Builder mainPacket(Object mainPacket) {
            this.mainPacket = mainPacket;
            return this;
        }

        public Builder extraPackets(Object... extraPackets) {
            if (extraPackets == null || extraPackets.length == 0) {
                return this;
            }

            if (this.extraPackets == null) {
                this.extraPackets = new ArrayList<>(extraPackets.length);
            }
            this.extraPackets.add(extraPackets);
            return this;
        }

        public Builder extraPackets(Collection<?> extraPackets) {
            if (extraPackets == null || extraPackets.isEmpty()) {
                return this;
            }

            if (this.extraPackets == null) {
                this.extraPackets = new ArrayList<>(extraPackets.size());
            }
            this.extraPackets.addAll(extraPackets);
            return this;
        }

        public PacketContainer build() {
            if (this.extraPackets != null) {
                return new PacketContainer(this.mainPacket, this.extraPackets.size()).addAll(this.extraPackets);
            } else {
                return new PacketContainer(this.mainPacket);
            }
        }

    }

}
