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
import org.imanity.framework.bukkit.util.reflection.Reflection;
import org.imanity.framework.bukkit.util.reflection.resolver.FieldResolver;

public final class WrappedPacketInAbilities extends WrappedPacket {
    private static boolean MULTIPLE_ABILITIES;
    private boolean isVulnerable;
    private boolean isFlying;
    private boolean allowFly;
    private boolean instantBuild;
    private float flySpeed;
    private float walkSpeed;

    public WrappedPacketInAbilities(Object packet) {
        super(packet);
    }

    static {
        MULTIPLE_ABILITIES = Reflection.getField(PacketTypeClasses.Client.ABILITIES, boolean.class, 1) != null;
    }

    @Override
    protected void setup() {
        if (MULTIPLE_ABILITIES) {
            this.isVulnerable = readBoolean(0);
            this.isFlying = readBoolean(1);
            this.allowFly = readBoolean(2);
            this.instantBuild = readBoolean(3);
            this.flySpeed = readFloat(0);
            this.walkSpeed = readFloat(1);
        } else {
            this.isFlying = readBoolean(0);
        }
    }

    /**
     * This will return null if the server version is not available in 1.16.x and above
     *
     * @return Whether the player is vulnerable to damage or not.
     */
    @Deprecated
    public Boolean isVulnerable() {
        return isVulnerable;
    }

    public boolean isFlying() {
        return isFlying;
    }

    /**
     * This will return null if the server version is not available in 1.16.x and above
     *
     * @return Whether or not the player can fly.
     */
    @Deprecated
    public Boolean isFlightAllowed() {
        return allowFly;
    }

    /**
     * This will return null if the server version is not available in 1.16.x and above
     *
     * @return Whether or not the player can break blocks instantly.
     */
    @Deprecated
    public Boolean canInstantlyBuild() {
        return instantBuild;
    }

    /**
     * This will return null if the server version is not available in 1.16.x and above
     *
     * @return The speed at which the player can fly, as a float.
     */
    @Deprecated
    public Float getFlySpeed() {
        return flySpeed;
    }

    /**
     * This will return null if the server version is not available in 1.16.x and above
     *
     * @return The speed at which the player can walk, as a float.
     */
    @Deprecated
    public Float getWalkSpeed() {
        return walkSpeed;
    }
}
