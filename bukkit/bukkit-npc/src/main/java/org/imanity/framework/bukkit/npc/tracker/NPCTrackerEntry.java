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

package org.imanity.framework.bukkit.npc.tracker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.npc.NPC;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NPCTrackerEntry {

    private final NPC npc;
    private int lastX;
    private int lastY;
    private int lastZ;
    private int lastYaw;
    private int lastPitch;
    private int lastHeadRotation;

    private int renderTick = 0;
    private int teleportDelay = 0;

    private boolean lastOnGround;

    private final Map<Player, Boolean> trackedPlayers = new ConcurrentHashMap<>();
    private final List<Player> toRemove = new ArrayList<>();

    public NPCTrackerEntry(NPC npc) {
        this.npc = npc;

        Location location = npc.getLocation();
        this.lastX = (int) Math.floor(location.getX() * 32.0D);
        this.lastY = (int) Math.floor(location.getY() * 32.0D);
        this.lastZ = (int) Math.floor(location.getZ() * 32.0D);
        this.lastYaw = (int) Math.floor(location.getYaw() * 256.0D / 360.0F);
        this.lastPitch = (int) Math.floor(location.getPitch() * 256.0D / 360.0F);
        this.lastHeadRotation = (int) Math.floor(location.getYaw() * 256.0D / 360.0F);
        this.lastOnGround = npc.isOnGround();
    }

    public Set<Player> getTrackedPlayers() {
        return this.trackedPlayers.keySet();
    }

    public boolean isTracked(Player player) {
        return this.trackedPlayers.containsKey(player);
    }

    public void render() {
        this.removeFarPlayers();
        this.addNearbyPlayers();

        ++this.teleportDelay;

        if (this.renderTick % 2 == 0) {

            Location location = this.npc.getLocation();
            int dataX = (int) Math.floor(location.getX() * 32.0D);
            int dataY = (int) Math.floor(location.getY() * 32.0D);
            int dataZ = (int) Math.floor(location.getZ() * 32.0D);
            int dataYaw = (int) Math.floor(location.getYaw() * 256.0F / 360.0F);
            int dataPitch = (int) Math.floor(location.getPitch() * 256.0F / 360.0F);


            int diffX = dataX - this.lastX;
            int diffY = dataY - this.lastY;
            int diffZ = dataZ - this.lastZ;
            PacketContainer packetContainer = null;

            boolean shouldUpdateLocation = Math.abs(diffX) >= 4 || Math.abs(diffY) >= 4 || Math.abs(diffZ) >= 4 || this.renderTick % 60 == 0;
            boolean shouldUpdateRotation = Math.abs(dataYaw - this.lastYaw) >= 4 || Math.abs(dataPitch - this.lastPitch) >= 4;

            if (renderTick > 0) {

                if (shouldUpdateLocation) {
                    this.lastX = dataX;
                    this.lastY = dataY;
                    this.lastZ = dataZ;
                }

                if (shouldUpdateRotation) {
                    this.lastYaw = dataYaw;
                    this.lastPitch = dataPitch;
                }

                if (diffX >= -128 && diffX < 128 && diffY >= -128 && diffY < 128 && diffZ >= -128 && diffZ < 128 && this.teleportDelay <= 400 && this.lastOnGround == this.npc.isOnGround()) {

                    if (!shouldUpdateLocation || !shouldUpdateRotation) {

                        if (shouldUpdateLocation) {
                            packetContainer = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
                            packetContainer.getIntegers().write(0, this.npc.getEntityId());
                            packetContainer.getBytes()
                                    .write(0, (byte) diffX)
                                    .write(1, (byte) diffY)
                                    .write(2, (byte) diffZ);
                            packetContainer.getBooleans()
                                    .write(0, this.npc.isOnGround());
                        } else if (shouldUpdateRotation) {
                            packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
                            packetContainer.getIntegers().write(0, this.npc.getEntityId());
                            packetContainer.getBytes()
                                    .write(3, (byte) dataYaw)
                                    .write(4, (byte) dataPitch);
                            packetContainer.getBooleans()
                                    .write(0, this.npc.isOnGround())
                                    .write(1, true);
                        }

                    } else {
                        packetContainer = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
                        packetContainer.getIntegers().write(0, this.npc.getEntityId());
                        packetContainer.getBytes()
                                .write(0, (byte) diffX)
                                .write(1, (byte) diffY)
                                .write(2, (byte) diffZ)
                                .write(3, (byte) dataYaw)
                                .write(4, (byte) dataPitch);
                        packetContainer.getBooleans()
                                .write(0, this.npc.isOnGround())
                                .write(1, true);
                    }

                } else {

                    this.lastOnGround = this.npc.isOnGround();
                    this.teleportDelay = 0;

                    packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                    packetContainer.getIntegers()
                            .write(0, this.npc.getEntityId())
                            .write(1, dataX)
                            .write(2, dataY)
                            .write(3, dataZ);

                    packetContainer.getBytes()
                            .write(0, (byte) dataYaw)
                            .write(1, (byte) dataPitch);

                    packetContainer.getBooleans()
                            .write(0, this.npc.isOnGround());

                }
            }

            if (packetContainer != null) {

                if (packetContainer.getType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                    this.broadcast(packetContainer);
                } else {
                    PacketContainer teleportPacket = null;

                    for (Map.Entry<Player, Boolean> viewer : this.trackedPlayers.entrySet()) {
                        if (viewer.getValue()) {
                            viewer.setValue(false);
                            if (teleportPacket == null) {
                                teleportPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                                teleportPacket.getIntegers()
                                        .write(0, this.npc.getEntityId())
                                        .write(1, dataX)
                                        .write(2, dataY)
                                        .write(3, dataZ);

                                teleportPacket.getBytes()
                                        .write(0, (byte) dataYaw)
                                        .write(1, (byte) dataPitch);

                                teleportPacket.getBooleans()
                                        .write(0, this.npc.isOnGround());
                            }

                            this.sendPacket(viewer.getKey(), teleportPacket);
                        } else {
                            this.sendPacket(viewer.getKey(), packetContainer);
                        }
                    }

                }

            }

            int headRotation = (int) Math.floor(this.npc.getLocation().getYaw() * 256.0F / 360.0F);
            if (Math.abs(headRotation - this.lastHeadRotation) >= 4) {
                PacketContainer headPacket = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
                headPacket.getIntegers().write(0, this.npc.getEntityId());
                headPacket.getBytes().write(0, (byte) headRotation);
                this.broadcast(headPacket);

                this.lastHeadRotation = headRotation;
            }

        }

        ++this.renderTick;
    }

    private void sendPacket(Player player, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcast(PacketContainer packetContainer) {
        for (Player player : this.trackedPlayers.keySet()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addNearbyPlayers() {

        Imanity.IMPLEMENTATION.getPlayerRadius(this.npc.getLocation(), this.npc.getPool().getSpawnDistance()).forEach(player -> {

            if (!this.trackedPlayers.containsKey(player)) {
                this.trackedPlayers.put(player, true);

                this.npc.show(player);
            }

        });

    }

    public void untrack(Player player) {
        this.npc.hide(player);

        this.trackedPlayers.remove(player);
    }

    public void removeFarPlayers() {
        for (Player player : this.trackedPlayers.keySet()) {
            double x = player.getLocation().getX() - this.npc.getLocation().getX();
            double z = player.getLocation().getZ() - this.npc.getLocation().getZ();

            if (Math.abs(x * x) > this.npc.getPool().getSpawnDistance() || Math.abs(z * z) > this.npc.getPool().getSpawnDistance()) {
                toRemove.add(player);
            }
        }

        for (Player player : this.toRemove) {
            this.untrack(player);
        }

        this.toRemove.clear();
    }

    public void untrack() {
        for (Player player : this.trackedPlayers.keySet()) {
            this.npc.hide(player);
            this.trackedPlayers.remove(player);
        }
    }

}
