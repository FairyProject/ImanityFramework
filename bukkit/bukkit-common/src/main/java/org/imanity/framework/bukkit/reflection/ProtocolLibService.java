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

package org.imanity.framework.bukkit.reflection;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.imanity.framework.PreInitialize;
import org.imanity.framework.Service;
import org.imanity.framework.util.CC;

@Service(name = "protocollib")
public class ProtocolLibService {

    private static final Logger LOGGER = LogManager.getLogger(ProtocolLibService.class);

    private ProtocolManager protocolManager;
    private boolean enabled;

    @PreInitialize
    public void onInitialize() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            LOGGER.warn(CC.YELLOW + "The ProtocolLib plugin hasn't been installed! Some feature may not work in this case!");
            return;
        }

        this.enabled = true;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void validEnabled() {
        if (!this.enabled) {
            throw new IllegalArgumentException("ProtocolLib isn't enabled! this feature couldn't work!");
        }
    }

    public void send(Player player, PacketContainer packetContainer) {
        try {
            this.protocolManager.sendServerPacket(player, packetContainer);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Error while sending packet", throwable);
        }
    }

}
