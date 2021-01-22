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

package org.imanity.framework.bukkit.npc;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.npc.goal.Goal;
import org.imanity.framework.bukkit.npc.goal.LookAtPlayerGoal;
import org.imanity.framework.bukkit.npc.modifier.*;
import org.imanity.framework.bukkit.npc.profile.Profile;
import org.imanity.framework.bukkit.npc.tracker.NPCTrackerEntry;
import org.imanity.framework.bukkit.npc.util.AxisAlignedBB;
import org.imanity.framework.util.FastRandom;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 *
 * @credit https://github.com/juliarn/NPC-Lib
 * @modified by LeeGod
 *
 */
@Getter
public class NPC {

    private static final Random RANDOM = new FastRandom();

    private final NPCTrackerEntry trackerEntry;

    private final int entityId = RANDOM.nextInt(Short.MAX_VALUE);

    private final WrappedGameProfile gameProfile;

    private Location location;
    private Vector velocity;

    private AxisAlignedBB boundingBox;

    private int noDamageTicks;

    private boolean sneaking;
    private boolean sprinting;
    private boolean imitatePlayer;

    private final float width;
    private final float height;

    private float jumpMovementFactor = 0.02F;

    private final NPCPool pool;
    private final SpawnCustomizer spawnCustomizer;

    private final List<Goal> goals = new ArrayList<>();

    private NPC(NPCPool pool, WrappedGameProfile gameProfile, Location location, boolean imitatePlayer, SpawnCustomizer spawnCustomizer) {
        this.pool = pool;
        this.gameProfile = gameProfile;

        this.location = location;
        this.velocity = new Vector();
        this.imitatePlayer = imitatePlayer;
        this.spawnCustomizer = spawnCustomizer;

        this.trackerEntry = new NPCTrackerEntry(this);

        this.width = 0.6F;
        this.height = 1.8F;
    }

    public void addGoal(Goal goal) {
        this.goals.add(goal);
    }

    public void tick() {

        for (Goal goal : this.goals) {
            goal.tick();
        }

    }

    public void render() {
        this.trackerEntry.render();
    }

    public void look(Location location) {
        double xDifference = location.getX() - this.getLocation().getX();
        double yDifference = location.getY() - this.getLocation().getY();
        double zDifference = location.getZ() - this.getLocation().getZ();

        double r = Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2) + Math.pow(zDifference, 2));

        float yaw = (float) (-Math.atan2(xDifference, zDifference) / Math.PI * 180D);
        yaw = yaw < 0 ? yaw + 360 : yaw;

        float pitch = (float) (-Math.asin(yDifference / r) / Math.PI * 180D);

        this.location.setYaw(yaw);
        this.location.setPitch(pitch);
    }

    public MaterialData getCurrentBlock() {
        return this.pool.getBlockAt(this.location);
    }

    public boolean isCurrentBlock(Material... material) {
        return this.pool.isMaterialInBoundingBox(this.boundingBox.expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), material);
    }

    public boolean isInWater() {
        return this.isCurrentBlock(Material.WATER, Material.STATIONARY_WATER);
    }

    public boolean isInLava() {
        return this.isCurrentBlock(Material.LAVA, Material.STATIONARY_LAVA);
    }

    public void move(float moveForward, float moveStrafe) {

        double y;

        if (this.isInWater()) {
            y = this.location.getY();

            this.moveFlying(moveForward, moveStrafe, 0.02F);
            this.motion(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
            this.velocity.multiply(0.800000011920929D);
            this.velocity.setY(this.velocity.getY() - 0.02);
        } else if (this.isInLava()) {

            y = this.location.getY();

            this.moveFlying(moveForward, moveStrafe, 0.02F);
            this.motion(this.velocity.getX(), this.velocity.getY(), this.velocity.getZ());
            this.velocity.multiply(0.5D);
            this.velocity.setY(this.velocity.getY() - 0.02);

        } else {

            float multiply = 0.91F;
            if (this.isOnGround()) {
                multiply = Imanity.IMPLEMENTATION.getBlockSlipperiness(this.pool.getBlockAt(this.getBlockX(), ((int) Math.floor(this.boundingBox.minY)) - 1, this.getBlockZ()).getItemType()) * 0.91F;
            }

            float f = 0.16277136F / (multiply * multiply * multiply);
            float speed = this.isOnGround() ? 0.699999988079071F * f : this.jumpMovementFactor;

            this.moveFlying(moveForward, moveStrafe, speed);

        }

    }

    public void moveFlying(float moveForward, float moveStrafe, float multiply) {
        float f = moveForward * moveForward + moveStrafe * moveStrafe;

        if (f >= 1.0E-4F) {
            f = (float) Math.sqrt(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = multiply / f;
            moveForward *= f;
            moveStrafe *= f;

            float motX = (float) Math.sin(this.getLocation().getYaw() * Math.PI / 180.0F);
            float motZ = (float) Math.sin(this.getLocation().getYaw() * Math.PI / 180.0F);
            this.velocity.add(new Vector(moveForward * motZ - moveStrafe * motX, 0, moveStrafe * motZ + moveForward * motX));
        }
    }

    protected void motion(double motX, double motY, double motZ) {

    }

    public void show(@NotNull Player player) {
        VisibilityModifier visibilityModifier = new VisibilityModifier(this);
        visibilityModifier.queuePlayerListChange(EnumWrappers.PlayerInfoAction.ADD_PLAYER).send(player);

        Imanity.TASK_CHAIN_FACTORY
                .newChain()
                .delay(10)
                .sync(() -> {
                    visibilityModifier.queueSpawn().send(player);
                    this.spawnCustomizer.handleSpawn(this, player);
                })
                .delay((int) this.pool.getTabListRemoveTicks())
                .sync(() -> visibilityModifier.queuePlayerListChange(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER).send(player))
        .execute();
    }

    public void hide(@NotNull Player player) {
        new VisibilityModifier(this)
                .queuePlayerListChange(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
                .queueDestroy()
                .send(player);
    }

    public void untrack(Player player) {
        this.trackerEntry.untrack(player);
    }

    public boolean isOnGround() {
        return true; // TODO
    }

    /**
     * @return a copy of all players seeing this NPC
     */
    public Collection<Player> getSeeingPlayers() {
        return new HashSet<>(this.trackerEntry.getTrackedPlayers());
    }

    public boolean isShownFor(Player player) {
        return this.trackerEntry.isTracked(player);
    }

    public void setPosition(double x, double y, double z) {
        Location location = this.getLocation().clone();
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        this.location = location;

        float f = this.width / 2.0F;
        float f1 = this.height;

        this.setBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    public void setPosition(Location location) {
        location = location.clone();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        this.rotate(location.getYaw(), location.getPitch());
    }

    private void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public double getX() {
        return this.getLocation().getX();
    }

    public double getY() {
        return this.getLocation().getY();
    }

    public double getZ() {
        return this.getLocation().getZ();
    }

    public int getBlockX() {
        return (int) Math.floor(this.getX());
    }

    public int getBlockY() {
        return (int) Math.floor(this.getY());
    }

    public int getBlockZ() {
        return (int) Math.floor(this.getZ());
    }

    /**
     * Creates a new animation modifier which serves methods to play animations on an NPC
     *
     * @return a animation modifier modifying this NPC
     */
    public AnimationModifier animation() {
        return new AnimationModifier(this);
    }

    /**
     * Creates a new rotation modifier which serves methods related to entity rotation
     *
     * @return a rotation modifier modifying this NPC
     */
    public void rotate(float yaw, float pitch) {
        this.location.setYaw(yaw);
        this.location.setPitch(pitch);
    }

    /**
     * Creates a new equipemt modifier which serves methods to change an NPCs equipment
     *
     * @return an equipment modifier modifying this NPC
     */
    public EquipmentModifier equipment() {
        return new EquipmentModifier(this);
    }

    /**
     * Creates a new metadata modifier which serves methods to change an NPCs metadata, including sneaking etc.
     *
     * @return a metadata modifier modifying this NPC
     */
    public MetadataModifier metadata() {
        return new MetadataModifier(this);
    }

    @NotNull
    public WrappedGameProfile getGameProfile() {
        return gameProfile;
    }

    public int getEntityId() {
        return entityId;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public boolean isImitatePlayer() {
        return imitatePlayer;
    }

    public void setImitatePlayer(boolean imitatePlayer) {
        this.imitatePlayer = imitatePlayer;
    }

    public World getWorld() {
        return this.location.getWorld();
    }


    public static class Builder {

        private final Profile profile;

        private Location location = new Location(Bukkit.getWorlds().get(0), 0D, 0D, 0D);

        private boolean lookAtPlayer = true;

        private boolean imitatePlayer = true;

        private SpawnCustomizer spawnCustomizer = (npc, player) -> {
        };

        /**
         * Creates a new instance of the NPC builder
         *
         * @param profile a player profile defining UUID, name and textures of the NPC
         */
        public Builder(@NotNull Profile profile) {
            this.profile = profile;
        }

        /**
         * Sets the location of the npc, cannot be changed afterwards
         *
         * @param location the location
         * @return this builder instance
         */
        public Builder location(@NotNull Location location) {
            this.location = location;
            return this;
        }

        /**
         * Enables/disables looking at the player, default is true
         *
         * @param lookAtPlayer if the NPC should look at the player
         * @return this builder instance
         */
        public Builder lookAtPlayer(boolean lookAtPlayer) {
            this.lookAtPlayer = lookAtPlayer;
            return this;
        }

        /**
         * Enables/disables imitation of the player, such as sneaking and hitting the player, default is true
         *
         * @param imitatePlayer if the NPC should imitate players
         * @return this builder instance
         */
        public Builder imitatePlayer(boolean imitatePlayer) {
            this.imitatePlayer = imitatePlayer;
            return this;
        }

        /**
         * Sets an executor which will be called every time the NPC is spawned for a certain player.
         * Permanent NPC modifications should be done in this method, otherwise they will be lost at the next respawn of the NPC.
         *
         * @param spawnCustomizer the spawn customizer which will be called on every spawn
         * @return this builder instance
         */
        public Builder spawnCustomizer(@NotNull SpawnCustomizer spawnCustomizer) {
            this.spawnCustomizer = spawnCustomizer;
            return this;
        }

        /**
         * Passes the NPC to a pool which handles events, spawning and destruction of this NPC for players
         *
         * @param pool the pool the NPC will be passed to
         * @return this builder instance
         */
        @NotNull
        public NPC build(@NotNull NPCPool pool) {
            if (!this.profile.isComplete()) {
                throw new IllegalStateException("The provided profile has to be complete!");
            }

            NPC npc = new NPC(
                    pool,
                    this.profile.asWrapped(),
                    this.location,
                    this.imitatePlayer,
                    this.spawnCustomizer
            );
            if (this.lookAtPlayer) {
                npc.addGoal(new LookAtPlayerGoal(npc, 15));
            }

            pool.takeCareOf(npc);

            return npc;
        }

    }

}
