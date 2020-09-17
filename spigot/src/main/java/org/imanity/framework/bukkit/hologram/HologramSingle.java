package org.imanity.framework.bukkit.hologram;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.hologram.api.ViewHandler;
import org.imanity.framework.bukkit.util.SpigotUtil;
import org.imanity.framework.bukkit.reflection.MinecraftReflection;

import java.util.Collection;

@Getter
@Setter
public class HologramSingle {

    private Hologram hologram;

    private int index;
    private int skullId;
    private int horseId;
    private float y;

    private ViewHandler viewHandler;

    public HologramSingle(Hologram hologram, ViewHandler viewHandler, float y, int index) {
        this.hologram = hologram;
        this.y = y;
        this.viewHandler = viewHandler;
        this.index = index;

        this.skullId = MinecraftReflection.getNewEntityId();
        this.horseId = MinecraftReflection.getNewEntityId();
    }

    public int getSkullId() {
        return this.skullId;
    }

    public Location getLocation() {
        return hologram.getLocation().clone().add(0, this.y, 0);
    }

    public void send(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendSpawnPacket(players);
            this.sendTeleportPacket(players);
            this.sendNamePackets(players);
            this.sendAttachPacket(players);
        }

    }

    public void sendRemove(Collection<? extends Player> players) {
        if (!players.isEmpty()) {
            this.sendDestroyPacket(players);
        }

    }

    protected void sendSpawnPacket(Collection<? extends Player> players) {

        players.forEach(player -> Imanity.IMPLEMENTATION.sendHologramSpawnPacket(player, this));

    }

    protected void sendTeleportPacket(Collection<? extends Player> players) {
        players.forEach(player -> {

            if (MinecraftReflection.MINECRAFT_VERSION.newerThan(MinecraftReflection.Version.v1_7_R4) || SpigotUtil.getProtocolVersion(player) > 5) {
                Imanity.IMPLEMENTATION.sendEntityTeleport(player, this.getLocation().add(0, -2.25, 0), this.skullId);
            } else {
                Imanity.IMPLEMENTATION.sendEntityTeleport(player, this.getLocation().add(0, 54.56D, 0), this.skullId);
                Imanity.IMPLEMENTATION.sendEntityTeleport(player, this.getLocation().add(0, 54.56D, 0), this.horseId);
            }

        });
    }

    protected void sendNamePackets(Collection<? extends Player> players) {
        players.forEach(player -> Imanity.IMPLEMENTATION.sendHologramNamePacket(player, this));
    }

    protected void sendDestroyPacket(Collection<? extends Player> players) {
        players.forEach(player -> Imanity.IMPLEMENTATION.sendEntityDestroy(player, this.skullId, this.horseId));
    }

    protected void sendAttachPacket(Collection<? extends Player> players) {
        players.forEach(player -> {
            if (this.hologram.isAttached()) {
                Imanity.IMPLEMENTATION.sendEntityAttach(player, 0, this.skullId, this.hologram.getAttachedTo().getEntityId());
            } else {
                Imanity.IMPLEMENTATION.sendEntityAttach(player, 0, this.skullId, -1);
            }
        });
    }

}